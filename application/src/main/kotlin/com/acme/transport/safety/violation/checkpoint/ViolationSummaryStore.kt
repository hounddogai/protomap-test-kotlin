package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation summaries by code. */
interface ViolationSummaryStore {
    fun find(code: String): ViolationSummary?
    fun save(item: ViolationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationSummaryStore(
    var items: MutableMap<String, ViolationSummary>,
    var order: MutableList<String>,
) : ViolationSummaryStore {
    override fun find(code: String): ViolationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationSummary) {
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

    override fun list(): MutableList<ViolationSummary> {
        val out = mutableListOf<ViolationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationSummaryStore(): ViolationSummaryStore {
    return MemoryViolationSummaryStore(mutableMapOf<String, ViolationSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationSummaryStore(
    var inner: ViolationSummaryStore,
    var cache: TtlCache<ViolationSummary>,
    var hits: Int,
    var misses: Int,
) : ViolationSummaryStore {
    override fun find(code: String): ViolationSummary? {
        val hit = (cache.get(code) as ViolationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationSummary> = inner.list()
}

fun newCachedViolationSummaryStore(inner: ViolationSummaryStore, ttl: Int, clock: Clock): ViolationSummaryStore {
    return CachedViolationSummaryStore(
        inner = inner,
        cache = newTtlCache<ViolationSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation summaries. Empty fields match all. */
data class ViolationSummaryQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<ViolationSummary>): MutableList<ViolationSummary> {
        val matched = filter(items) { candidate: ViolationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationSummaries(store: ViolationSummaryStore, query: ViolationSummaryQuery): MutableList<ViolationSummary> {
    return query.apply(store.list())
}
