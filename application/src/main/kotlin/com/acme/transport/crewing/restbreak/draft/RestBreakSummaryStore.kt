package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break summaries by code. */
interface RestBreakSummaryStore {
    fun find(code: String): RestBreakSummary?
    fun save(item: RestBreakSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakSummaryStore(
    var items: MutableMap<String, RestBreakSummary>,
    var order: MutableList<String>,
) : RestBreakSummaryStore {
    override fun find(code: String): RestBreakSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakSummary) {
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

    override fun list(): MutableList<RestBreakSummary> {
        val out = mutableListOf<RestBreakSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakSummaryStore(): RestBreakSummaryStore {
    return MemoryRestBreakSummaryStore(mutableMapOf<String, RestBreakSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakSummaryStore(
    var inner: RestBreakSummaryStore,
    var cache: TtlCache<RestBreakSummary>,
    var hits: Int,
    var misses: Int,
) : RestBreakSummaryStore {
    override fun find(code: String): RestBreakSummary? {
        val hit = (cache.get(code) as RestBreakSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakSummary> = inner.list()
}

fun newCachedRestBreakSummaryStore(inner: RestBreakSummaryStore, ttl: Int, clock: Clock): RestBreakSummaryStore {
    return CachedRestBreakSummaryStore(
        inner = inner,
        cache = newTtlCache<RestBreakSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break summaries. Empty fields match all. */
data class RestBreakSummaryQuery(
    var source: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakSummary>): MutableList<RestBreakSummary> {
        val matched = filter(items) { candidate: RestBreakSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakSummaries(store: RestBreakSummaryStore, query: RestBreakSummaryQuery): MutableList<RestBreakSummary> {
    return query.apply(store.list())
}
