package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient incident proposals by code. */
interface LenientIncidentProposalStore {
    fun find(code: String): LenientIncidentProposal?
    fun save(item: LenientIncidentProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIncidentProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIncidentProposalStore(
    var items: MutableMap<String, LenientIncidentProposal>,
    var order: MutableList<String>,
) : LenientIncidentProposalStore {
    override fun find(code: String): LenientIncidentProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIncidentProposal) {
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

    override fun list(): MutableList<LenientIncidentProposal> {
        val out = mutableListOf<LenientIncidentProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIncidentProposalStore(): LenientIncidentProposalStore {
    return MemoryLenientIncidentProposalStore(
        mutableMapOf<String, LenientIncidentProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIncidentProposalStore(
    var inner: LenientIncidentProposalStore,
    var cache: TtlCache<LenientIncidentProposal>,
    var hits: Int,
    var misses: Int,
) : LenientIncidentProposalStore {
    override fun find(code: String): LenientIncidentProposal? {
        val hit = (cache.get(code) as LenientIncidentProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIncidentProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIncidentProposal> = inner.list()
}

fun newCachedLenientIncidentProposalStore(inner: LenientIncidentProposalStore, ttl: Int, clock: Clock): LenientIncidentProposalStore {
    return CachedLenientIncidentProposalStore(
        inner = inner,
        cache = newTtlCache<LenientIncidentProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient incident proposals. Empty fields match all. */
data class LenientIncidentProposalQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIncidentProposal): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIncidentProposal>): MutableList<LenientIncidentProposal> {
        val matched = filter(items) { candidate: LenientIncidentProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIncidentProposals(store: LenientIncidentProposalStore, query: LenientIncidentProposalQuery): MutableList<LenientIncidentProposal> {
    return query.apply(store.list())
}
