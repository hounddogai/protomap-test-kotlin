package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional sortation adjustments by code. */
interface RegionalSortationAdjustmentStore {
    fun find(code: String): RegionalSortationAdjustment?
    fun save(item: RegionalSortationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSortationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSortationAdjustmentStore(
    var items: MutableMap<String, RegionalSortationAdjustment>,
    var order: MutableList<String>,
) : RegionalSortationAdjustmentStore {
    override fun find(code: String): RegionalSortationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSortationAdjustment) {
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

    override fun list(): MutableList<RegionalSortationAdjustment> {
        val out = mutableListOf<RegionalSortationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSortationAdjustmentStore(): RegionalSortationAdjustmentStore {
    return MemoryRegionalSortationAdjustmentStore(
        mutableMapOf<String, RegionalSortationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSortationAdjustmentStore(
    var inner: RegionalSortationAdjustmentStore,
    var cache: TtlCache<RegionalSortationAdjustment>,
    var hits: Int,
    var misses: Int,
) : RegionalSortationAdjustmentStore {
    override fun find(code: String): RegionalSortationAdjustment? {
        val hit = (cache.get(code) as RegionalSortationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSortationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSortationAdjustment> = inner.list()
}

fun newCachedRegionalSortationAdjustmentStore(inner: RegionalSortationAdjustmentStore, ttl: Int, clock: Clock): RegionalSortationAdjustmentStore {
    return CachedRegionalSortationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RegionalSortationAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional sortation adjustments. Empty fields match all. */
data class RegionalSortationAdjustmentQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSortationAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalSortationAdjustment>): MutableList<RegionalSortationAdjustment> {
        val matched = filter(items) { candidate: RegionalSortationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSortationAdjustments(store: RegionalSortationAdjustmentStore, query: RegionalSortationAdjustmentQuery): MutableList<RegionalSortationAdjustment> {
    return query.apply(store.list())
}
