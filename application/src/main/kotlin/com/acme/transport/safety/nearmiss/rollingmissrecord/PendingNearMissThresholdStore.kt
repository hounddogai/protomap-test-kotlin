package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending near miss thresholds by code. */
interface PendingNearMissThresholdStore {
    fun find(code: String): PendingNearMissThreshold?
    fun save(item: PendingNearMissThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNearMissThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNearMissThresholdStore(
    var items: MutableMap<String, PendingNearMissThreshold>,
    var order: MutableList<String>,
) : PendingNearMissThresholdStore {
    override fun find(code: String): PendingNearMissThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNearMissThreshold) {
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

    override fun list(): MutableList<PendingNearMissThreshold> {
        val out = mutableListOf<PendingNearMissThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNearMissThresholdStore(): PendingNearMissThresholdStore {
    return MemoryPendingNearMissThresholdStore(
        mutableMapOf<String, PendingNearMissThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNearMissThresholdStore(
    var inner: PendingNearMissThresholdStore,
    var cache: TtlCache<PendingNearMissThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingNearMissThresholdStore {
    override fun find(code: String): PendingNearMissThreshold? {
        val hit = (cache.get(code) as PendingNearMissThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNearMissThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNearMissThreshold> = inner.list()
}

fun newCachedPendingNearMissThresholdStore(inner: PendingNearMissThresholdStore, ttl: Int, clock: Clock): PendingNearMissThresholdStore {
    return CachedPendingNearMissThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingNearMissThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending near miss thresholds. Empty fields match all. */
data class PendingNearMissThresholdQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNearMissThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNearMissThreshold>): MutableList<PendingNearMissThreshold> {
        val matched = filter(items) { candidate: PendingNearMissThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNearMissThresholds(store: PendingNearMissThresholdStore, query: PendingNearMissThresholdQuery): MutableList<PendingNearMissThreshold> {
    return query.apply(store.list())
}
