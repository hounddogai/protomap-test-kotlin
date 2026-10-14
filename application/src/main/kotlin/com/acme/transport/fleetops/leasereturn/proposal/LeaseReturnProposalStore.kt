package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return proposals by code. */
interface LeaseReturnProposalStore {
    fun find(code: String): LeaseReturnProposal?
    fun save(item: LeaseReturnProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnProposalStore(
    var items: MutableMap<String, LeaseReturnProposal>,
    var order: MutableList<String>,
) : LeaseReturnProposalStore {
    override fun find(code: String): LeaseReturnProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnProposal) {
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

    override fun list(): MutableList<LeaseReturnProposal> {
        val out = mutableListOf<LeaseReturnProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnProposalStore(): LeaseReturnProposalStore {
    return MemoryLeaseReturnProposalStore(mutableMapOf<String, LeaseReturnProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnProposalStore(
    var inner: LeaseReturnProposalStore,
    var cache: TtlCache<LeaseReturnProposal>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnProposalStore {
    override fun find(code: String): LeaseReturnProposal? {
        val hit = (cache.get(code) as LeaseReturnProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnProposal> = inner.list()
}

fun newCachedLeaseReturnProposalStore(inner: LeaseReturnProposalStore, ttl: Int, clock: Clock): LeaseReturnProposalStore {
    return CachedLeaseReturnProposalStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return proposals. Empty fields match all. */
data class LeaseReturnProposalQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnProposal): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnProposal>): MutableList<LeaseReturnProposal> {
        val matched = filter(items) { candidate: LeaseReturnProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnProposals(store: LeaseReturnProposalStore, query: LeaseReturnProposalQuery): MutableList<LeaseReturnProposal> {
    return query.apply(store.list())
}
