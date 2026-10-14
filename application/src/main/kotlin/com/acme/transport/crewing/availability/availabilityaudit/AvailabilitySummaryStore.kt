package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability summaries by code. */
interface AvailabilitySummaryStore {
    fun find(code: String): AvailabilitySummary?
    fun save(item: AvailabilitySummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilitySummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilitySummaryStore(
    var items: MutableMap<String, AvailabilitySummary>,
    var order: MutableList<String>,
) : AvailabilitySummaryStore {
    override fun find(code: String): AvailabilitySummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilitySummary) {
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

    override fun list(): MutableList<AvailabilitySummary> {
        val out = mutableListOf<AvailabilitySummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilitySummaryStore(): AvailabilitySummaryStore {
    return MemoryAvailabilitySummaryStore(mutableMapOf<String, AvailabilitySummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilitySummaryStore(
    var inner: AvailabilitySummaryStore,
    var cache: TtlCache<AvailabilitySummary>,
    var hits: Int,
    var misses: Int,
) : AvailabilitySummaryStore {
    override fun find(code: String): AvailabilitySummary? {
        val hit = (cache.get(code) as AvailabilitySummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilitySummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilitySummary> = inner.list()
}

fun newCachedAvailabilitySummaryStore(inner: AvailabilitySummaryStore, ttl: Int, clock: Clock): AvailabilitySummaryStore {
    return CachedAvailabilitySummaryStore(
        inner = inner,
        cache = newTtlCache<AvailabilitySummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability summaries. Empty fields match all. */
data class AvailabilitySummaryQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilitySummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilitySummary>): MutableList<AvailabilitySummary> {
        val matched = filter(items) { candidate: AvailabilitySummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilitySummaries(store: AvailabilitySummaryStore, query: AvailabilitySummaryQuery): MutableList<AvailabilitySummary> {
    return query.apply(store.list())
}
