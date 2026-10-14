package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery proposals by code. */
interface ProofOfDeliveryProposalStore {
    fun find(code: String): ProofOfDeliveryProposal?
    fun save(item: ProofOfDeliveryProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryProposalStore(
    var items: MutableMap<String, ProofOfDeliveryProposal>,
    var order: MutableList<String>,
) : ProofOfDeliveryProposalStore {
    override fun find(code: String): ProofOfDeliveryProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryProposal) {
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

    override fun list(): MutableList<ProofOfDeliveryProposal> {
        val out = mutableListOf<ProofOfDeliveryProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryProposalStore(): ProofOfDeliveryProposalStore {
    return MemoryProofOfDeliveryProposalStore(
        mutableMapOf<String, ProofOfDeliveryProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryProposalStore(
    var inner: ProofOfDeliveryProposalStore,
    var cache: TtlCache<ProofOfDeliveryProposal>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryProposalStore {
    override fun find(code: String): ProofOfDeliveryProposal? {
        val hit = (cache.get(code) as ProofOfDeliveryProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryProposal> = inner.list()
}

fun newCachedProofOfDeliveryProposalStore(inner: ProofOfDeliveryProposalStore, ttl: Int, clock: Clock): ProofOfDeliveryProposalStore {
    return CachedProofOfDeliveryProposalStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery proposals. Empty fields match all. */
data class ProofOfDeliveryProposalQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryProposal>): MutableList<ProofOfDeliveryProposal> {
        val matched = filter(items) { candidate: ProofOfDeliveryProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryProposals(store: ProofOfDeliveryProposalStore, query: ProofOfDeliveryProposalQuery): MutableList<ProofOfDeliveryProposal> {
    return query.apply(store.list())
}
