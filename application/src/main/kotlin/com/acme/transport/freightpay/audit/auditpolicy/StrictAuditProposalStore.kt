package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict audit proposals by code. */
interface StrictAuditProposalStore {
    fun find(code: String): StrictAuditProposal?
    fun save(item: StrictAuditProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAuditProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAuditProposalStore(
    var items: MutableMap<String, StrictAuditProposal>,
    var order: MutableList<String>,
) : StrictAuditProposalStore {
    override fun find(code: String): StrictAuditProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAuditProposal) {
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

    override fun list(): MutableList<StrictAuditProposal> {
        val out = mutableListOf<StrictAuditProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAuditProposalStore(): StrictAuditProposalStore {
    return MemoryStrictAuditProposalStore(mutableMapOf<String, StrictAuditProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAuditProposalStore(
    var inner: StrictAuditProposalStore,
    var cache: TtlCache<StrictAuditProposal>,
    var hits: Int,
    var misses: Int,
) : StrictAuditProposalStore {
    override fun find(code: String): StrictAuditProposal? {
        val hit = (cache.get(code) as StrictAuditProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAuditProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAuditProposal> = inner.list()
}

fun newCachedStrictAuditProposalStore(inner: StrictAuditProposalStore, ttl: Int, clock: Clock): StrictAuditProposalStore {
    return CachedStrictAuditProposalStore(
        inner = inner,
        cache = newTtlCache<StrictAuditProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict audit proposals. Empty fields match all. */
data class StrictAuditProposalQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAuditProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictAuditProposal>): MutableList<StrictAuditProposal> {
        val matched = filter(items) { candidate: StrictAuditProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAuditProposals(store: StrictAuditProposalStore, query: StrictAuditProposalQuery): MutableList<StrictAuditProposal> {
    return query.apply(store.list())
}
