package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge summaries by code. */
interface EvChargeSummaryStore {
    fun find(code: String): EvChargeSummary?
    fun save(item: EvChargeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeSummaryStore(
    var items: MutableMap<String, EvChargeSummary>,
    var order: MutableList<String>,
) : EvChargeSummaryStore {
    override fun find(code: String): EvChargeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeSummary) {
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

    override fun list(): MutableList<EvChargeSummary> {
        val out = mutableListOf<EvChargeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeSummaryStore(): EvChargeSummaryStore {
    return MemoryEvChargeSummaryStore(mutableMapOf<String, EvChargeSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeSummaryStore(
    var inner: EvChargeSummaryStore,
    var cache: TtlCache<EvChargeSummary>,
    var hits: Int,
    var misses: Int,
) : EvChargeSummaryStore {
    override fun find(code: String): EvChargeSummary? {
        val hit = (cache.get(code) as EvChargeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeSummary> = inner.list()
}

fun newCachedEvChargeSummaryStore(inner: EvChargeSummaryStore, ttl: Int, clock: Clock): EvChargeSummaryStore {
    return CachedEvChargeSummaryStore(
        inner = inner,
        cache = newTtlCache<EvChargeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge summaries. Empty fields match all. */
data class EvChargeSummaryQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeSummary>): MutableList<EvChargeSummary> {
        val matched = filter(items) { candidate: EvChargeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeSummaries(store: EvChargeSummaryStore, query: EvChargeSummaryQuery): MutableList<EvChargeSummary> {
    return query.apply(store.list())
}
