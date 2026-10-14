package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold summaries by code. */
interface HoldSummaryStore {
    fun find(code: String): HoldSummary?
    fun save(item: HoldSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldSummaryStore(
    var items: MutableMap<String, HoldSummary>,
    var order: MutableList<String>,
) : HoldSummaryStore {
    override fun find(code: String): HoldSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldSummary) {
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

    override fun list(): MutableList<HoldSummary> {
        val out = mutableListOf<HoldSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldSummaryStore(): HoldSummaryStore {
    return MemoryHoldSummaryStore(mutableMapOf<String, HoldSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldSummaryStore(
    var inner: HoldSummaryStore,
    var cache: TtlCache<HoldSummary>,
    var hits: Int,
    var misses: Int,
) : HoldSummaryStore {
    override fun find(code: String): HoldSummary? {
        val hit = (cache.get(code) as HoldSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldSummary> = inner.list()
}

fun newCachedHoldSummaryStore(inner: HoldSummaryStore, ttl: Int, clock: Clock): HoldSummaryStore {
    return CachedHoldSummaryStore(
        inner = inner,
        cache = newTtlCache<HoldSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold summaries. Empty fields match all. */
data class HoldSummaryQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldSummary>): MutableList<HoldSummary> {
        val matched = filter(items) { candidate: HoldSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldSummaries(store: HoldSummaryStore, query: HoldSummaryQuery): MutableList<HoldSummary> {
    return query.apply(store.list())
}
