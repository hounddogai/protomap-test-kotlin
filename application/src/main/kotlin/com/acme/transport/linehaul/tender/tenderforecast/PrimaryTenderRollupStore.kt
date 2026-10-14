package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender rollups by code. */
interface PrimaryTenderRollupStore {
    fun find(code: String): PrimaryTenderRollup?
    fun save(item: PrimaryTenderRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderRollupStore(
    var items: MutableMap<String, PrimaryTenderRollup>,
    var order: MutableList<String>,
) : PrimaryTenderRollupStore {
    override fun find(code: String): PrimaryTenderRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderRollup) {
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

    override fun list(): MutableList<PrimaryTenderRollup> {
        val out = mutableListOf<PrimaryTenderRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderRollupStore(): PrimaryTenderRollupStore {
    return MemoryPrimaryTenderRollupStore(mutableMapOf<String, PrimaryTenderRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderRollupStore(
    var inner: PrimaryTenderRollupStore,
    var cache: TtlCache<PrimaryTenderRollup>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderRollupStore {
    override fun find(code: String): PrimaryTenderRollup? {
        val hit = (cache.get(code) as PrimaryTenderRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderRollup> = inner.list()
}

fun newCachedPrimaryTenderRollupStore(inner: PrimaryTenderRollupStore, ttl: Int, clock: Clock): PrimaryTenderRollupStore {
    return CachedPrimaryTenderRollupStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender rollups. Empty fields match all. */
data class PrimaryTenderRollupQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTenderRollup>): MutableList<PrimaryTenderRollup> {
        val matched = filter(items) { candidate: PrimaryTenderRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderRollups(store: PrimaryTenderRollupStore, query: PrimaryTenderRollupQuery): MutableList<PrimaryTenderRollup> {
    return query.apply(store.list())
}
