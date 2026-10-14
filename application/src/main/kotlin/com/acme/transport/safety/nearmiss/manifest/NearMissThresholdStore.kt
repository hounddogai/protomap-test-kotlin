package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss thresholds by code. */
interface NearMissThresholdStore {
    fun find(code: String): NearMissThreshold?
    fun save(item: NearMissThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissThresholdStore(
    var items: MutableMap<String, NearMissThreshold>,
    var order: MutableList<String>,
) : NearMissThresholdStore {
    override fun find(code: String): NearMissThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissThreshold) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<NearMissThreshold> {
        val out = mutableListOf<NearMissThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissThresholdStore(): NearMissThresholdStore {
    return MemoryNearMissThresholdStore(mutableMapOf<String, NearMissThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissThresholdStore(
    var inner: NearMissThresholdStore,
    var cache: TtlCache<NearMissThreshold>,
    var hits: Int,
    var misses: Int,
) : NearMissThresholdStore {
    override fun find(code: String): NearMissThreshold? {
        val hit = (cache.get(code) as NearMissThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissThreshold> = inner.list()
}

fun newCachedNearMissThresholdStore(inner: NearMissThresholdStore, ttl: Int, clock: Clock): NearMissThresholdStore {
    return CachedNearMissThresholdStore(
        inner = inner,
        cache = newTtlCache<NearMissThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss thresholds. Empty fields match all. */
data class NearMissThresholdQuery(
    var unit: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissThreshold>): MutableList<NearMissThreshold> {
        val matched = filter(items) { candidate: NearMissThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissThresholds(store: NearMissThresholdStore, query: NearMissThresholdQuery): MutableList<NearMissThreshold> {
    return query.apply(store.list())
}
