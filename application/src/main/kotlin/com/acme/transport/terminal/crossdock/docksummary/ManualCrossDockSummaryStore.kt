package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual cross dock summaries by code. */
interface ManualCrossDockSummaryStore {
    fun find(code: String): ManualCrossDockSummary?
    fun save(item: ManualCrossDockSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCrossDockSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCrossDockSummaryStore(
    var items: MutableMap<String, ManualCrossDockSummary>,
    var order: MutableList<String>,
) : ManualCrossDockSummaryStore {
    override fun find(code: String): ManualCrossDockSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCrossDockSummary) {
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

    override fun list(): MutableList<ManualCrossDockSummary> {
        val out = mutableListOf<ManualCrossDockSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCrossDockSummaryStore(): ManualCrossDockSummaryStore {
    return MemoryManualCrossDockSummaryStore(
        mutableMapOf<String, ManualCrossDockSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCrossDockSummaryStore(
    var inner: ManualCrossDockSummaryStore,
    var cache: TtlCache<ManualCrossDockSummary>,
    var hits: Int,
    var misses: Int,
) : ManualCrossDockSummaryStore {
    override fun find(code: String): ManualCrossDockSummary? {
        val hit = (cache.get(code) as ManualCrossDockSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCrossDockSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCrossDockSummary> = inner.list()
}

fun newCachedManualCrossDockSummaryStore(inner: ManualCrossDockSummaryStore, ttl: Int, clock: Clock): ManualCrossDockSummaryStore {
    return CachedManualCrossDockSummaryStore(
        inner = inner,
        cache = newTtlCache<ManualCrossDockSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual cross dock summaries. Empty fields match all. */
data class ManualCrossDockSummaryQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCrossDockSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCrossDockSummary>): MutableList<ManualCrossDockSummary> {
        val matched = filter(items) { candidate: ManualCrossDockSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCrossDockSummaries(store: ManualCrossDockSummaryStore, query: ManualCrossDockSummaryQuery): MutableList<ManualCrossDockSummary> {
    return query.apply(store.list())
}
