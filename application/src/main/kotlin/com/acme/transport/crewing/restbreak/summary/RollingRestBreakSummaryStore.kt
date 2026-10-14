package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rest break summaries by code. */
interface RollingRestBreakSummaryStore {
    fun find(code: String): RollingRestBreakSummary?
    fun save(item: RollingRestBreakSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRestBreakSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRestBreakSummaryStore(
    var items: MutableMap<String, RollingRestBreakSummary>,
    var order: MutableList<String>,
) : RollingRestBreakSummaryStore {
    override fun find(code: String): RollingRestBreakSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRestBreakSummary) {
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

    override fun list(): MutableList<RollingRestBreakSummary> {
        val out = mutableListOf<RollingRestBreakSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRestBreakSummaryStore(): RollingRestBreakSummaryStore {
    return MemoryRollingRestBreakSummaryStore(
        mutableMapOf<String, RollingRestBreakSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRestBreakSummaryStore(
    var inner: RollingRestBreakSummaryStore,
    var cache: TtlCache<RollingRestBreakSummary>,
    var hits: Int,
    var misses: Int,
) : RollingRestBreakSummaryStore {
    override fun find(code: String): RollingRestBreakSummary? {
        val hit = (cache.get(code) as RollingRestBreakSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRestBreakSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRestBreakSummary> = inner.list()
}

fun newCachedRollingRestBreakSummaryStore(inner: RollingRestBreakSummaryStore, ttl: Int, clock: Clock): RollingRestBreakSummaryStore {
    return CachedRollingRestBreakSummaryStore(
        inner = inner,
        cache = newTtlCache<RollingRestBreakSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rest break summaries. Empty fields match all. */
data class RollingRestBreakSummaryQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRestBreakSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingRestBreakSummary>): MutableList<RollingRestBreakSummary> {
        val matched = filter(items) { candidate: RollingRestBreakSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRestBreakSummaries(store: RollingRestBreakSummaryStore, query: RollingRestBreakSummaryQuery): MutableList<RollingRestBreakSummary> {
    return query.apply(store.list())
}
