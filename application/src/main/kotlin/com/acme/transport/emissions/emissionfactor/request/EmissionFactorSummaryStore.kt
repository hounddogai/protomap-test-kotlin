package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor summaries by code. */
interface EmissionFactorSummaryStore {
    fun find(code: String): EmissionFactorSummary?
    fun save(item: EmissionFactorSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorSummaryStore(
    var items: MutableMap<String, EmissionFactorSummary>,
    var order: MutableList<String>,
) : EmissionFactorSummaryStore {
    override fun find(code: String): EmissionFactorSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorSummary) {
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

    override fun list(): MutableList<EmissionFactorSummary> {
        val out = mutableListOf<EmissionFactorSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorSummaryStore(): EmissionFactorSummaryStore {
    return MemoryEmissionFactorSummaryStore(
        mutableMapOf<String, EmissionFactorSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorSummaryStore(
    var inner: EmissionFactorSummaryStore,
    var cache: TtlCache<EmissionFactorSummary>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorSummaryStore {
    override fun find(code: String): EmissionFactorSummary? {
        val hit = (cache.get(code) as EmissionFactorSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorSummary> = inner.list()
}

fun newCachedEmissionFactorSummaryStore(inner: EmissionFactorSummaryStore, ttl: Int, clock: Clock): EmissionFactorSummaryStore {
    return CachedEmissionFactorSummaryStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor summaries. Empty fields match all. */
data class EmissionFactorSummaryQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorSummary>): MutableList<EmissionFactorSummary> {
        val matched = filter(items) { candidate: EmissionFactorSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorSummaries(store: EmissionFactorSummaryStore, query: EmissionFactorSummaryQuery): MutableList<EmissionFactorSummary> {
    return query.apply(store.list())
}
