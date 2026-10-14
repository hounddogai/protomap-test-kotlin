package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit proposals by code. */
interface AuditProposalStore {
    fun find(code: String): AuditProposal?
    fun save(item: AuditProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditProposalStore(
    var items: MutableMap<String, AuditProposal>,
    var order: MutableList<String>,
) : AuditProposalStore {
    override fun find(code: String): AuditProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditProposal) {
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

    override fun list(): MutableList<AuditProposal> {
        val out = mutableListOf<AuditProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditProposalStore(): AuditProposalStore {
    return MemoryAuditProposalStore(mutableMapOf<String, AuditProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditProposalStore(
    var inner: AuditProposalStore,
    var cache: TtlCache<AuditProposal>,
    var hits: Int,
    var misses: Int,
) : AuditProposalStore {
    override fun find(code: String): AuditProposal? {
        val hit = (cache.get(code) as AuditProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditProposal> = inner.list()
}

fun newCachedAuditProposalStore(inner: AuditProposalStore, ttl: Int, clock: Clock): AuditProposalStore {
    return CachedAuditProposalStore(
        inner = inner,
        cache = newTtlCache<AuditProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit proposals. Empty fields match all. */
data class AuditProposalQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditProposal): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<AuditProposal>): MutableList<AuditProposal> {
        val matched = filter(items) { candidate: AuditProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditProposals(store: AuditProposalStore, query: AuditProposalQuery): MutableList<AuditProposal> {
    return query.apply(store.list())
}
