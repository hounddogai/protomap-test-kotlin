package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict dispute summaries by code. */
interface StrictDisputeSummaryStore {
    fun find(code: String): StrictDisputeSummary?
    fun save(item: StrictDisputeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDisputeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDisputeSummaryStore(
    var items: MutableMap<String, StrictDisputeSummary>,
    var order: MutableList<String>,
) : StrictDisputeSummaryStore {
    override fun find(code: String): StrictDisputeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDisputeSummary) {
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

    override fun list(): MutableList<StrictDisputeSummary> {
        val out = mutableListOf<StrictDisputeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDisputeSummaryStore(): StrictDisputeSummaryStore {
    return MemoryStrictDisputeSummaryStore(
        mutableMapOf<String, StrictDisputeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDisputeSummaryStore(
    var inner: StrictDisputeSummaryStore,
    var cache: TtlCache<StrictDisputeSummary>,
    var hits: Int,
    var misses: Int,
) : StrictDisputeSummaryStore {
    override fun find(code: String): StrictDisputeSummary? {
        val hit = (cache.get(code) as StrictDisputeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDisputeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDisputeSummary> = inner.list()
}

fun newCachedStrictDisputeSummaryStore(inner: StrictDisputeSummaryStore, ttl: Int, clock: Clock): StrictDisputeSummaryStore {
    return CachedStrictDisputeSummaryStore(
        inner = inner,
        cache = newTtlCache<StrictDisputeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dispute summaries. Empty fields match all. */
data class StrictDisputeSummaryQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDisputeSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<StrictDisputeSummary>): MutableList<StrictDisputeSummary> {
        val matched = filter(items) { candidate: StrictDisputeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDisputeSummaries(store: StrictDisputeSummaryStore, query: StrictDisputeSummaryQuery): MutableList<StrictDisputeSummary> {
    return query.apply(store.list())
}
