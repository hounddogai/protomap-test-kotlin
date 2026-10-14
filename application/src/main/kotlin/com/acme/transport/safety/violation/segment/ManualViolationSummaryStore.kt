package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual violation summaries by code. */
interface ManualViolationSummaryStore {
    fun find(code: String): ManualViolationSummary?
    fun save(item: ManualViolationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualViolationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualViolationSummaryStore(
    var items: MutableMap<String, ManualViolationSummary>,
    var order: MutableList<String>,
) : ManualViolationSummaryStore {
    override fun find(code: String): ManualViolationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualViolationSummary) {
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

    override fun list(): MutableList<ManualViolationSummary> {
        val out = mutableListOf<ManualViolationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualViolationSummaryStore(): ManualViolationSummaryStore {
    return MemoryManualViolationSummaryStore(
        mutableMapOf<String, ManualViolationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualViolationSummaryStore(
    var inner: ManualViolationSummaryStore,
    var cache: TtlCache<ManualViolationSummary>,
    var hits: Int,
    var misses: Int,
) : ManualViolationSummaryStore {
    override fun find(code: String): ManualViolationSummary? {
        val hit = (cache.get(code) as ManualViolationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualViolationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualViolationSummary> = inner.list()
}

fun newCachedManualViolationSummaryStore(inner: ManualViolationSummaryStore, ttl: Int, clock: Clock): ManualViolationSummaryStore {
    return CachedManualViolationSummaryStore(
        inner = inner,
        cache = newTtlCache<ManualViolationSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual violation summaries. Empty fields match all. */
data class ManualViolationSummaryQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualViolationSummary): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualViolationSummary>): MutableList<ManualViolationSummary> {
        val matched = filter(items) { candidate: ManualViolationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualViolationSummaries(store: ManualViolationSummaryStore, query: ManualViolationSummaryQuery): MutableList<ManualViolationSummary> {
    return query.apply(store.list())
}
