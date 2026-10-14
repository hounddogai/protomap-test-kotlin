package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped availability rollups by code. */
interface CappedAvailabilityRollupStore {
    fun find(code: String): CappedAvailabilityRollup?
    fun save(item: CappedAvailabilityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAvailabilityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAvailabilityRollupStore(
    var items: MutableMap<String, CappedAvailabilityRollup>,
    var order: MutableList<String>,
) : CappedAvailabilityRollupStore {
    override fun find(code: String): CappedAvailabilityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAvailabilityRollup) {
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

    override fun list(): MutableList<CappedAvailabilityRollup> {
        val out = mutableListOf<CappedAvailabilityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAvailabilityRollupStore(): CappedAvailabilityRollupStore {
    return MemoryCappedAvailabilityRollupStore(
        mutableMapOf<String, CappedAvailabilityRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAvailabilityRollupStore(
    var inner: CappedAvailabilityRollupStore,
    var cache: TtlCache<CappedAvailabilityRollup>,
    var hits: Int,
    var misses: Int,
) : CappedAvailabilityRollupStore {
    override fun find(code: String): CappedAvailabilityRollup? {
        val hit = (cache.get(code) as CappedAvailabilityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAvailabilityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAvailabilityRollup> = inner.list()
}

fun newCachedCappedAvailabilityRollupStore(inner: CappedAvailabilityRollupStore, ttl: Int, clock: Clock): CappedAvailabilityRollupStore {
    return CachedCappedAvailabilityRollupStore(
        inner = inner,
        cache = newTtlCache<CappedAvailabilityRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped availability rollups. Empty fields match all. */
data class CappedAvailabilityRollupQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAvailabilityRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAvailabilityRollup>): MutableList<CappedAvailabilityRollup> {
        val matched = filter(items) { candidate: CappedAvailabilityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAvailabilityRollups(store: CappedAvailabilityRollupStore, query: CappedAvailabilityRollupQuery): MutableList<CappedAvailabilityRollup> {
    return query.apply(store.list())
}
