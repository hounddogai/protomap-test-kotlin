package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard adjustments by code. */
interface RegionalTrailerYardAdjustmentStore {
    fun find(code: String): RegionalTrailerYardAdjustment?
    fun save(item: RegionalTrailerYardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardAdjustmentStore(
    var items: MutableMap<String, RegionalTrailerYardAdjustment>,
    var order: MutableList<String>,
) : RegionalTrailerYardAdjustmentStore {
    override fun find(code: String): RegionalTrailerYardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardAdjustment) {
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

    override fun list(): MutableList<RegionalTrailerYardAdjustment> {
        val out = mutableListOf<RegionalTrailerYardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardAdjustmentStore(): RegionalTrailerYardAdjustmentStore {
    return MemoryRegionalTrailerYardAdjustmentStore(
        mutableMapOf<String, RegionalTrailerYardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardAdjustmentStore(
    var inner: RegionalTrailerYardAdjustmentStore,
    var cache: TtlCache<RegionalTrailerYardAdjustment>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardAdjustmentStore {
    override fun find(code: String): RegionalTrailerYardAdjustment? {
        val hit = (cache.get(code) as RegionalTrailerYardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardAdjustment> = inner.list()
}

fun newCachedRegionalTrailerYardAdjustmentStore(inner: RegionalTrailerYardAdjustmentStore, ttl: Int, clock: Clock): RegionalTrailerYardAdjustmentStore {
    return CachedRegionalTrailerYardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard adjustments. Empty fields match all. */
data class RegionalTrailerYardAdjustmentQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardAdjustment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerYardAdjustment>): MutableList<RegionalTrailerYardAdjustment> {
        val matched = filter(items) { candidate: RegionalTrailerYardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardAdjustments(store: RegionalTrailerYardAdjustmentStore, query: RegionalTrailerYardAdjustmentQuery): MutableList<RegionalTrailerYardAdjustment> {
    return query.apply(store.list())
}
