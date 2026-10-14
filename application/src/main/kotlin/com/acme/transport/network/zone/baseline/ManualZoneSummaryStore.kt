package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual zone summaries by code. */
interface ManualZoneSummaryStore {
    fun find(code: String): ManualZoneSummary?
    fun save(item: ManualZoneSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualZoneSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualZoneSummaryStore(
    var items: MutableMap<String, ManualZoneSummary>,
    var order: MutableList<String>,
) : ManualZoneSummaryStore {
    override fun find(code: String): ManualZoneSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualZoneSummary) {
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

    override fun list(): MutableList<ManualZoneSummary> {
        val out = mutableListOf<ManualZoneSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualZoneSummaryStore(): ManualZoneSummaryStore {
    return MemoryManualZoneSummaryStore(mutableMapOf<String, ManualZoneSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualZoneSummaryStore(
    var inner: ManualZoneSummaryStore,
    var cache: TtlCache<ManualZoneSummary>,
    var hits: Int,
    var misses: Int,
) : ManualZoneSummaryStore {
    override fun find(code: String): ManualZoneSummary? {
        val hit = (cache.get(code) as ManualZoneSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualZoneSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualZoneSummary> = inner.list()
}

fun newCachedManualZoneSummaryStore(inner: ManualZoneSummaryStore, ttl: Int, clock: Clock): ManualZoneSummaryStore {
    return CachedManualZoneSummaryStore(
        inner = inner,
        cache = newTtlCache<ManualZoneSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual zone summaries. Empty fields match all. */
data class ManualZoneSummaryQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualZoneSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualZoneSummary>): MutableList<ManualZoneSummary> {
        val matched = filter(items) { candidate: ManualZoneSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualZoneSummaries(store: ManualZoneSummaryStore, query: ManualZoneSummaryQuery): MutableList<ManualZoneSummary> {
    return query.apply(store.list())
}
