package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global emission factor summaries by code. */
interface GlobalEmissionFactorSummaryStore {
    fun find(code: String): GlobalEmissionFactorSummary?
    fun save(item: GlobalEmissionFactorSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalEmissionFactorSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalEmissionFactorSummaryStore(
    var items: MutableMap<String, GlobalEmissionFactorSummary>,
    var order: MutableList<String>,
) : GlobalEmissionFactorSummaryStore {
    override fun find(code: String): GlobalEmissionFactorSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalEmissionFactorSummary) {
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

    override fun list(): MutableList<GlobalEmissionFactorSummary> {
        val out = mutableListOf<GlobalEmissionFactorSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalEmissionFactorSummaryStore(): GlobalEmissionFactorSummaryStore {
    return MemoryGlobalEmissionFactorSummaryStore(
        mutableMapOf<String, GlobalEmissionFactorSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalEmissionFactorSummaryStore(
    var inner: GlobalEmissionFactorSummaryStore,
    var cache: TtlCache<GlobalEmissionFactorSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalEmissionFactorSummaryStore {
    override fun find(code: String): GlobalEmissionFactorSummary? {
        val hit = (cache.get(code) as GlobalEmissionFactorSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalEmissionFactorSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalEmissionFactorSummary> = inner.list()
}

fun newCachedGlobalEmissionFactorSummaryStore(inner: GlobalEmissionFactorSummaryStore, ttl: Int, clock: Clock): GlobalEmissionFactorSummaryStore {
    return CachedGlobalEmissionFactorSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalEmissionFactorSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global emission factor summaries. Empty fields match all. */
data class GlobalEmissionFactorSummaryQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalEmissionFactorSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalEmissionFactorSummary>): MutableList<GlobalEmissionFactorSummary> {
        val matched = filter(items) { candidate: GlobalEmissionFactorSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalEmissionFactorSummaries(store: GlobalEmissionFactorSummaryStore, query: GlobalEmissionFactorSummaryQuery): MutableList<GlobalEmissionFactorSummary> {
    return query.apply(store.list())
}
