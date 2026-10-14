package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending hold summaries by code. */
interface PendingHoldSummaryStore {
    fun find(code: String): PendingHoldSummary?
    fun save(item: PendingHoldSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHoldSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHoldSummaryStore(
    var items: MutableMap<String, PendingHoldSummary>,
    var order: MutableList<String>,
) : PendingHoldSummaryStore {
    override fun find(code: String): PendingHoldSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHoldSummary) {
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

    override fun list(): MutableList<PendingHoldSummary> {
        val out = mutableListOf<PendingHoldSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHoldSummaryStore(): PendingHoldSummaryStore {
    return MemoryPendingHoldSummaryStore(mutableMapOf<String, PendingHoldSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHoldSummaryStore(
    var inner: PendingHoldSummaryStore,
    var cache: TtlCache<PendingHoldSummary>,
    var hits: Int,
    var misses: Int,
) : PendingHoldSummaryStore {
    override fun find(code: String): PendingHoldSummary? {
        val hit = (cache.get(code) as PendingHoldSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHoldSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHoldSummary> = inner.list()
}

fun newCachedPendingHoldSummaryStore(inner: PendingHoldSummaryStore, ttl: Int, clock: Clock): PendingHoldSummaryStore {
    return CachedPendingHoldSummaryStore(
        inner = inner,
        cache = newTtlCache<PendingHoldSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hold summaries. Empty fields match all. */
data class PendingHoldSummaryQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHoldSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<PendingHoldSummary>): MutableList<PendingHoldSummary> {
        val matched = filter(items) { candidate: PendingHoldSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHoldSummaries(store: PendingHoldSummaryStore, query: PendingHoldSummaryQuery): MutableList<PendingHoldSummary> {
    return query.apply(store.list())
}
