package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity summaries by code. */
interface CapacitySummaryStore {
    fun find(code: String): CapacitySummary?
    fun save(item: CapacitySummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacitySummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacitySummaryStore(
    var items: MutableMap<String, CapacitySummary>,
    var order: MutableList<String>,
) : CapacitySummaryStore {
    override fun find(code: String): CapacitySummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacitySummary) {
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

    override fun list(): MutableList<CapacitySummary> {
        val out = mutableListOf<CapacitySummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacitySummaryStore(): CapacitySummaryStore {
    return MemoryCapacitySummaryStore(mutableMapOf<String, CapacitySummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacitySummaryStore(
    var inner: CapacitySummaryStore,
    var cache: TtlCache<CapacitySummary>,
    var hits: Int,
    var misses: Int,
) : CapacitySummaryStore {
    override fun find(code: String): CapacitySummary? {
        val hit = (cache.get(code) as CapacitySummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacitySummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacitySummary> = inner.list()
}

fun newCachedCapacitySummaryStore(inner: CapacitySummaryStore, ttl: Int, clock: Clock): CapacitySummaryStore {
    return CachedCapacitySummaryStore(
        inner = inner,
        cache = newTtlCache<CapacitySummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity summaries. Empty fields match all. */
data class CapacitySummaryQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacitySummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacitySummary>): MutableList<CapacitySummary> {
        val matched = filter(items) { candidate: CapacitySummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacitySummaries(store: CapacitySummaryStore, query: CapacitySummaryQuery): MutableList<CapacitySummary> {
    return query.apply(store.list())
}
