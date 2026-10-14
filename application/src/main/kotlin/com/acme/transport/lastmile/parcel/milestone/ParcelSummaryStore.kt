package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel summaries by code. */
interface ParcelSummaryStore {
    fun find(code: String): ParcelSummary?
    fun save(item: ParcelSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelSummaryStore(
    var items: MutableMap<String, ParcelSummary>,
    var order: MutableList<String>,
) : ParcelSummaryStore {
    override fun find(code: String): ParcelSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelSummary) {
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

    override fun list(): MutableList<ParcelSummary> {
        val out = mutableListOf<ParcelSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelSummaryStore(): ParcelSummaryStore {
    return MemoryParcelSummaryStore(mutableMapOf<String, ParcelSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelSummaryStore(
    var inner: ParcelSummaryStore,
    var cache: TtlCache<ParcelSummary>,
    var hits: Int,
    var misses: Int,
) : ParcelSummaryStore {
    override fun find(code: String): ParcelSummary? {
        val hit = (cache.get(code) as ParcelSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelSummary> = inner.list()
}

fun newCachedParcelSummaryStore(inner: ParcelSummaryStore, ttl: Int, clock: Clock): ParcelSummaryStore {
    return CachedParcelSummaryStore(
        inner = inner,
        cache = newTtlCache<ParcelSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel summaries. Empty fields match all. */
data class ParcelSummaryQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelSummary>): MutableList<ParcelSummary> {
        val matched = filter(items) { candidate: ParcelSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelSummaries(store: ParcelSummaryStore, query: ParcelSummaryQuery): MutableList<ParcelSummary> {
    return query.apply(store.list())
}
