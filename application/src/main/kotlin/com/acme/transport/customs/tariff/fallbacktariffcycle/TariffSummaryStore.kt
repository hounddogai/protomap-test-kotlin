package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff summaries by code. */
interface TariffSummaryStore {
    fun find(code: String): TariffSummary?
    fun save(item: TariffSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffSummaryStore(
    var items: MutableMap<String, TariffSummary>,
    var order: MutableList<String>,
) : TariffSummaryStore {
    override fun find(code: String): TariffSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffSummary) {
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

    override fun list(): MutableList<TariffSummary> {
        val out = mutableListOf<TariffSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffSummaryStore(): TariffSummaryStore {
    return MemoryTariffSummaryStore(mutableMapOf<String, TariffSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffSummaryStore(
    var inner: TariffSummaryStore,
    var cache: TtlCache<TariffSummary>,
    var hits: Int,
    var misses: Int,
) : TariffSummaryStore {
    override fun find(code: String): TariffSummary? {
        val hit = (cache.get(code) as TariffSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffSummary> = inner.list()
}

fun newCachedTariffSummaryStore(inner: TariffSummaryStore, ttl: Int, clock: Clock): TariffSummaryStore {
    return CachedTariffSummaryStore(
        inner = inner,
        cache = newTtlCache<TariffSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff summaries. Empty fields match all. */
data class TariffSummaryQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<TariffSummary>): MutableList<TariffSummary> {
        val matched = filter(items) { candidate: TariffSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffSummaries(store: TariffSummaryStore, query: TariffSummaryQuery): MutableList<TariffSummary> {
    return query.apply(store.list())
}
