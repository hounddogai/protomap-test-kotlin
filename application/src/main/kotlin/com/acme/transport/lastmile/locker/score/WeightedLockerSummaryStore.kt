package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted locker summaries by code. */
interface WeightedLockerSummaryStore {
    fun find(code: String): WeightedLockerSummary?
    fun save(item: WeightedLockerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLockerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLockerSummaryStore(
    var items: MutableMap<String, WeightedLockerSummary>,
    var order: MutableList<String>,
) : WeightedLockerSummaryStore {
    override fun find(code: String): WeightedLockerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLockerSummary) {
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

    override fun list(): MutableList<WeightedLockerSummary> {
        val out = mutableListOf<WeightedLockerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLockerSummaryStore(): WeightedLockerSummaryStore {
    return MemoryWeightedLockerSummaryStore(
        mutableMapOf<String, WeightedLockerSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLockerSummaryStore(
    var inner: WeightedLockerSummaryStore,
    var cache: TtlCache<WeightedLockerSummary>,
    var hits: Int,
    var misses: Int,
) : WeightedLockerSummaryStore {
    override fun find(code: String): WeightedLockerSummary? {
        val hit = (cache.get(code) as WeightedLockerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLockerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLockerSummary> = inner.list()
}

fun newCachedWeightedLockerSummaryStore(inner: WeightedLockerSummaryStore, ttl: Int, clock: Clock): WeightedLockerSummaryStore {
    return CachedWeightedLockerSummaryStore(
        inner = inner,
        cache = newTtlCache<WeightedLockerSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted locker summaries. Empty fields match all. */
data class WeightedLockerSummaryQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLockerSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLockerSummary>): MutableList<WeightedLockerSummary> {
        val matched = filter(items) { candidate: WeightedLockerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLockerSummaries(store: WeightedLockerSummaryStore, query: WeightedLockerSummaryQuery): MutableList<WeightedLockerSummary> {
    return query.apply(store.list())
}
