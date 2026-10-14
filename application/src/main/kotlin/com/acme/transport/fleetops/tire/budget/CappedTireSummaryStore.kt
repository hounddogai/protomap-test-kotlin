package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tire summaries by code. */
interface CappedTireSummaryStore {
    fun find(code: String): CappedTireSummary?
    fun save(item: CappedTireSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTireSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTireSummaryStore(
    var items: MutableMap<String, CappedTireSummary>,
    var order: MutableList<String>,
) : CappedTireSummaryStore {
    override fun find(code: String): CappedTireSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTireSummary) {
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

    override fun list(): MutableList<CappedTireSummary> {
        val out = mutableListOf<CappedTireSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTireSummaryStore(): CappedTireSummaryStore {
    return MemoryCappedTireSummaryStore(mutableMapOf<String, CappedTireSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTireSummaryStore(
    var inner: CappedTireSummaryStore,
    var cache: TtlCache<CappedTireSummary>,
    var hits: Int,
    var misses: Int,
) : CappedTireSummaryStore {
    override fun find(code: String): CappedTireSummary? {
        val hit = (cache.get(code) as CappedTireSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTireSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTireSummary> = inner.list()
}

fun newCachedCappedTireSummaryStore(inner: CappedTireSummaryStore, ttl: Int, clock: Clock): CappedTireSummaryStore {
    return CachedCappedTireSummaryStore(
        inner = inner,
        cache = newTtlCache<CappedTireSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tire summaries. Empty fields match all. */
data class CappedTireSummaryQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTireSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CappedTireSummary>): MutableList<CappedTireSummary> {
        val matched = filter(items) { candidate: CappedTireSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTireSummaries(store: CappedTireSummaryStore, query: CappedTireSummaryQuery): MutableList<CappedTireSummary> {
    return query.apply(store.list())
}
