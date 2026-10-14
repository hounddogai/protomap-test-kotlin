package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity thresholds by code. */
interface CapacityThresholdStore {
    fun find(code: String): CapacityThreshold?
    fun save(item: CapacityThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityThresholdStore(
    var items: MutableMap<String, CapacityThreshold>,
    var order: MutableList<String>,
) : CapacityThresholdStore {
    override fun find(code: String): CapacityThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityThreshold) {
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

    override fun list(): MutableList<CapacityThreshold> {
        val out = mutableListOf<CapacityThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityThresholdStore(): CapacityThresholdStore {
    return MemoryCapacityThresholdStore(mutableMapOf<String, CapacityThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityThresholdStore(
    var inner: CapacityThresholdStore,
    var cache: TtlCache<CapacityThreshold>,
    var hits: Int,
    var misses: Int,
) : CapacityThresholdStore {
    override fun find(code: String): CapacityThreshold? {
        val hit = (cache.get(code) as CapacityThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityThreshold> = inner.list()
}

fun newCachedCapacityThresholdStore(inner: CapacityThresholdStore, ttl: Int, clock: Clock): CapacityThresholdStore {
    return CachedCapacityThresholdStore(
        inner = inner,
        cache = newTtlCache<CapacityThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity thresholds. Empty fields match all. */
data class CapacityThresholdQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityThreshold>): MutableList<CapacityThreshold> {
        val matched = filter(items) { candidate: CapacityThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityThresholds(store: CapacityThresholdStore, query: CapacityThresholdQuery): MutableList<CapacityThreshold> {
    return query.apply(store.list())
}
