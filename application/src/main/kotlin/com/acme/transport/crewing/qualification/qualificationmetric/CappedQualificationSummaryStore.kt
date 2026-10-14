package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped qualification summaries by code. */
interface CappedQualificationSummaryStore {
    fun find(code: String): CappedQualificationSummary?
    fun save(item: CappedQualificationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedQualificationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedQualificationSummaryStore(
    var items: MutableMap<String, CappedQualificationSummary>,
    var order: MutableList<String>,
) : CappedQualificationSummaryStore {
    override fun find(code: String): CappedQualificationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedQualificationSummary) {
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

    override fun list(): MutableList<CappedQualificationSummary> {
        val out = mutableListOf<CappedQualificationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedQualificationSummaryStore(): CappedQualificationSummaryStore {
    return MemoryCappedQualificationSummaryStore(
        mutableMapOf<String, CappedQualificationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedQualificationSummaryStore(
    var inner: CappedQualificationSummaryStore,
    var cache: TtlCache<CappedQualificationSummary>,
    var hits: Int,
    var misses: Int,
) : CappedQualificationSummaryStore {
    override fun find(code: String): CappedQualificationSummary? {
        val hit = (cache.get(code) as CappedQualificationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedQualificationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedQualificationSummary> = inner.list()
}

fun newCachedCappedQualificationSummaryStore(inner: CappedQualificationSummaryStore, ttl: Int, clock: Clock): CappedQualificationSummaryStore {
    return CachedCappedQualificationSummaryStore(
        inner = inner,
        cache = newTtlCache<CappedQualificationSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped qualification summaries. Empty fields match all. */
data class CappedQualificationSummaryQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedQualificationSummary): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<CappedQualificationSummary>): MutableList<CappedQualificationSummary> {
        val matched = filter(items) { candidate: CappedQualificationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedQualificationSummaries(store: CappedQualificationSummaryStore, query: CappedQualificationSummaryQuery): MutableList<CappedQualificationSummary> {
    return query.apply(store.list())
}
