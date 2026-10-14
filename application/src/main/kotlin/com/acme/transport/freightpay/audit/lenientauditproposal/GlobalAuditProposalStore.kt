package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global audit proposals by code. */
interface GlobalAuditProposalStore {
    fun find(code: String): GlobalAuditProposal?
    fun save(item: GlobalAuditProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAuditProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAuditProposalStore(
    var items: MutableMap<String, GlobalAuditProposal>,
    var order: MutableList<String>,
) : GlobalAuditProposalStore {
    override fun find(code: String): GlobalAuditProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAuditProposal) {
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

    override fun list(): MutableList<GlobalAuditProposal> {
        val out = mutableListOf<GlobalAuditProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAuditProposalStore(): GlobalAuditProposalStore {
    return MemoryGlobalAuditProposalStore(mutableMapOf<String, GlobalAuditProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAuditProposalStore(
    var inner: GlobalAuditProposalStore,
    var cache: TtlCache<GlobalAuditProposal>,
    var hits: Int,
    var misses: Int,
) : GlobalAuditProposalStore {
    override fun find(code: String): GlobalAuditProposal? {
        val hit = (cache.get(code) as GlobalAuditProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAuditProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAuditProposal> = inner.list()
}

fun newCachedGlobalAuditProposalStore(inner: GlobalAuditProposalStore, ttl: Int, clock: Clock): GlobalAuditProposalStore {
    return CachedGlobalAuditProposalStore(
        inner = inner,
        cache = newTtlCache<GlobalAuditProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global audit proposals. Empty fields match all. */
data class GlobalAuditProposalQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAuditProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<GlobalAuditProposal>): MutableList<GlobalAuditProposal> {
        val matched = filter(items) { candidate: GlobalAuditProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAuditProposals(store: GlobalAuditProposalStore, query: GlobalAuditProposalQuery): MutableList<GlobalAuditProposal> {
    return query.apply(store.list())
}
