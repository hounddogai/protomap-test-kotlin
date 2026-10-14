package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict carbon report proposals by code. */
interface StrictCarbonReportProposalStore {
    fun find(code: String): StrictCarbonReportProposal?
    fun save(item: StrictCarbonReportProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCarbonReportProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCarbonReportProposalStore(
    var items: MutableMap<String, StrictCarbonReportProposal>,
    var order: MutableList<String>,
) : StrictCarbonReportProposalStore {
    override fun find(code: String): StrictCarbonReportProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCarbonReportProposal) {
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

    override fun list(): MutableList<StrictCarbonReportProposal> {
        val out = mutableListOf<StrictCarbonReportProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCarbonReportProposalStore(): StrictCarbonReportProposalStore {
    return MemoryStrictCarbonReportProposalStore(
        mutableMapOf<String, StrictCarbonReportProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCarbonReportProposalStore(
    var inner: StrictCarbonReportProposalStore,
    var cache: TtlCache<StrictCarbonReportProposal>,
    var hits: Int,
    var misses: Int,
) : StrictCarbonReportProposalStore {
    override fun find(code: String): StrictCarbonReportProposal? {
        val hit = (cache.get(code) as StrictCarbonReportProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCarbonReportProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCarbonReportProposal> = inner.list()
}

fun newCachedStrictCarbonReportProposalStore(inner: StrictCarbonReportProposalStore, ttl: Int, clock: Clock): StrictCarbonReportProposalStore {
    return CachedStrictCarbonReportProposalStore(
        inner = inner,
        cache = newTtlCache<StrictCarbonReportProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict carbon report proposals. Empty fields match all. */
data class StrictCarbonReportProposalQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCarbonReportProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCarbonReportProposal>): MutableList<StrictCarbonReportProposal> {
        val matched = filter(items) { candidate: StrictCarbonReportProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCarbonReportProposals(store: StrictCarbonReportProposalStore, query: StrictCarbonReportProposalQuery): MutableList<StrictCarbonReportProposal> {
    return query.apply(store.list())
}
