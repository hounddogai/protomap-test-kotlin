package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report proposals by code. */
interface CarbonReportProposalStore {
    fun find(code: String): CarbonReportProposal?
    fun save(item: CarbonReportProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportProposalStore(
    var items: MutableMap<String, CarbonReportProposal>,
    var order: MutableList<String>,
) : CarbonReportProposalStore {
    override fun find(code: String): CarbonReportProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportProposal) {
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

    override fun list(): MutableList<CarbonReportProposal> {
        val out = mutableListOf<CarbonReportProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportProposalStore(): CarbonReportProposalStore {
    return MemoryCarbonReportProposalStore(
        mutableMapOf<String, CarbonReportProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportProposalStore(
    var inner: CarbonReportProposalStore,
    var cache: TtlCache<CarbonReportProposal>,
    var hits: Int,
    var misses: Int,
) : CarbonReportProposalStore {
    override fun find(code: String): CarbonReportProposal? {
        val hit = (cache.get(code) as CarbonReportProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportProposal> = inner.list()
}

fun newCachedCarbonReportProposalStore(inner: CarbonReportProposalStore, ttl: Int, clock: Clock): CarbonReportProposalStore {
    return CachedCarbonReportProposalStore(
        inner = inner,
        cache = newTtlCache<CarbonReportProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report proposals. Empty fields match all. */
data class CarbonReportProposalQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportProposal): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CarbonReportProposal>): MutableList<CarbonReportProposal> {
        val matched = filter(items) { candidate: CarbonReportProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportProposals(store: CarbonReportProposalStore, query: CarbonReportProposalQuery): MutableList<CarbonReportProposal> {
    return query.apply(store.list())
}
