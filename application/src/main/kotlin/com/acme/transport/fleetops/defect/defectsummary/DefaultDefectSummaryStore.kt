package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default defect summaries by code. */
interface DefaultDefectSummaryStore {
    fun find(code: String): DefaultDefectSummary?
    fun save(item: DefaultDefectSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDefectSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDefectSummaryStore(
    var items: MutableMap<String, DefaultDefectSummary>,
    var order: MutableList<String>,
) : DefaultDefectSummaryStore {
    override fun find(code: String): DefaultDefectSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDefectSummary) {
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

    override fun list(): MutableList<DefaultDefectSummary> {
        val out = mutableListOf<DefaultDefectSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDefectSummaryStore(): DefaultDefectSummaryStore {
    return MemoryDefaultDefectSummaryStore(
        mutableMapOf<String, DefaultDefectSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDefectSummaryStore(
    var inner: DefaultDefectSummaryStore,
    var cache: TtlCache<DefaultDefectSummary>,
    var hits: Int,
    var misses: Int,
) : DefaultDefectSummaryStore {
    override fun find(code: String): DefaultDefectSummary? {
        val hit = (cache.get(code) as DefaultDefectSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDefectSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDefectSummary> = inner.list()
}

fun newCachedDefaultDefectSummaryStore(inner: DefaultDefectSummaryStore, ttl: Int, clock: Clock): DefaultDefectSummaryStore {
    return CachedDefaultDefectSummaryStore(
        inner = inner,
        cache = newTtlCache<DefaultDefectSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default defect summaries. Empty fields match all. */
data class DefaultDefectSummaryQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDefectSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultDefectSummary>): MutableList<DefaultDefectSummary> {
        val matched = filter(items) { candidate: DefaultDefectSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDefectSummaries(store: DefaultDefectSummaryStore, query: DefaultDefectSummaryQuery): MutableList<DefaultDefectSummary> {
    return query.apply(store.list())
}
