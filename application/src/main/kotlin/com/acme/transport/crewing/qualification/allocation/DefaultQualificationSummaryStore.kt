package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default qualification summaries by code. */
interface DefaultQualificationSummaryStore {
    fun find(code: String): DefaultQualificationSummary?
    fun save(item: DefaultQualificationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultQualificationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultQualificationSummaryStore(
    var items: MutableMap<String, DefaultQualificationSummary>,
    var order: MutableList<String>,
) : DefaultQualificationSummaryStore {
    override fun find(code: String): DefaultQualificationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultQualificationSummary) {
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

    override fun list(): MutableList<DefaultQualificationSummary> {
        val out = mutableListOf<DefaultQualificationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultQualificationSummaryStore(): DefaultQualificationSummaryStore {
    return MemoryDefaultQualificationSummaryStore(
        mutableMapOf<String, DefaultQualificationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultQualificationSummaryStore(
    var inner: DefaultQualificationSummaryStore,
    var cache: TtlCache<DefaultQualificationSummary>,
    var hits: Int,
    var misses: Int,
) : DefaultQualificationSummaryStore {
    override fun find(code: String): DefaultQualificationSummary? {
        val hit = (cache.get(code) as DefaultQualificationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultQualificationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultQualificationSummary> = inner.list()
}

fun newCachedDefaultQualificationSummaryStore(inner: DefaultQualificationSummaryStore, ttl: Int, clock: Clock): DefaultQualificationSummaryStore {
    return CachedDefaultQualificationSummaryStore(
        inner = inner,
        cache = newTtlCache<DefaultQualificationSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default qualification summaries. Empty fields match all. */
data class DefaultQualificationSummaryQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultQualificationSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultQualificationSummary>): MutableList<DefaultQualificationSummary> {
        val matched = filter(items) { candidate: DefaultQualificationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultQualificationSummaries(store: DefaultQualificationSummaryStore, query: DefaultQualificationSummaryQuery): MutableList<DefaultQualificationSummary> {
    return query.apply(store.list())
}
