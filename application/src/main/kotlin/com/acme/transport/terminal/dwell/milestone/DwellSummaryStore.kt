package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell summaries by code. */
interface DwellSummaryStore {
    fun find(code: String): DwellSummary?
    fun save(item: DwellSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellSummaryStore(
    var items: MutableMap<String, DwellSummary>,
    var order: MutableList<String>,
) : DwellSummaryStore {
    override fun find(code: String): DwellSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellSummary) {
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

    override fun list(): MutableList<DwellSummary> {
        val out = mutableListOf<DwellSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellSummaryStore(): DwellSummaryStore {
    return MemoryDwellSummaryStore(mutableMapOf<String, DwellSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellSummaryStore(
    var inner: DwellSummaryStore,
    var cache: TtlCache<DwellSummary>,
    var hits: Int,
    var misses: Int,
) : DwellSummaryStore {
    override fun find(code: String): DwellSummary? {
        val hit = (cache.get(code) as DwellSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellSummary> = inner.list()
}

fun newCachedDwellSummaryStore(inner: DwellSummaryStore, ttl: Int, clock: Clock): DwellSummaryStore {
    return CachedDwellSummaryStore(
        inner = inner,
        cache = newTtlCache<DwellSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell summaries. Empty fields match all. */
data class DwellSummaryQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellSummary>): MutableList<DwellSummary> {
        val matched = filter(items) { candidate: DwellSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellSummaries(store: DwellSummaryStore, query: DwellSummaryQuery): MutableList<DwellSummary> {
    return query.apply(store.list())
}
