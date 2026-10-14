package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect proposals by code. */
interface DefectProposalStore {
    fun find(code: String): DefectProposal?
    fun save(item: DefectProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectProposalStore(
    var items: MutableMap<String, DefectProposal>,
    var order: MutableList<String>,
) : DefectProposalStore {
    override fun find(code: String): DefectProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectProposal) {
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

    override fun list(): MutableList<DefectProposal> {
        val out = mutableListOf<DefectProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectProposalStore(): DefectProposalStore {
    return MemoryDefectProposalStore(mutableMapOf<String, DefectProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectProposalStore(
    var inner: DefectProposalStore,
    var cache: TtlCache<DefectProposal>,
    var hits: Int,
    var misses: Int,
) : DefectProposalStore {
    override fun find(code: String): DefectProposal? {
        val hit = (cache.get(code) as DefectProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectProposal> = inner.list()
}

fun newCachedDefectProposalStore(inner: DefectProposalStore, ttl: Int, clock: Clock): DefectProposalStore {
    return CachedDefectProposalStore(
        inner = inner,
        cache = newTtlCache<DefectProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect proposals. Empty fields match all. */
data class DefectProposalQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectProposal): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectProposal>): MutableList<DefectProposal> {
        val matched = filter(items) { candidate: DefectProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectProposals(store: DefectProposalStore, query: DefectProposalQuery): MutableList<DefectProposal> {
    return query.apply(store.list())
}
