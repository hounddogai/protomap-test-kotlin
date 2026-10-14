package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled audit proposals by code. */
interface ScheduledAuditProposalStore {
    fun find(code: String): ScheduledAuditProposal?
    fun save(item: ScheduledAuditProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAuditProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAuditProposalStore(
    var items: MutableMap<String, ScheduledAuditProposal>,
    var order: MutableList<String>,
) : ScheduledAuditProposalStore {
    override fun find(code: String): ScheduledAuditProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAuditProposal) {
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

    override fun list(): MutableList<ScheduledAuditProposal> {
        val out = mutableListOf<ScheduledAuditProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAuditProposalStore(): ScheduledAuditProposalStore {
    return MemoryScheduledAuditProposalStore(
        mutableMapOf<String, ScheduledAuditProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAuditProposalStore(
    var inner: ScheduledAuditProposalStore,
    var cache: TtlCache<ScheduledAuditProposal>,
    var hits: Int,
    var misses: Int,
) : ScheduledAuditProposalStore {
    override fun find(code: String): ScheduledAuditProposal? {
        val hit = (cache.get(code) as ScheduledAuditProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAuditProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAuditProposal> = inner.list()
}

fun newCachedScheduledAuditProposalStore(inner: ScheduledAuditProposalStore, ttl: Int, clock: Clock): ScheduledAuditProposalStore {
    return CachedScheduledAuditProposalStore(
        inner = inner,
        cache = newTtlCache<ScheduledAuditProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled audit proposals. Empty fields match all. */
data class ScheduledAuditProposalQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAuditProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAuditProposal>): MutableList<ScheduledAuditProposal> {
        val matched = filter(items) { candidate: ScheduledAuditProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAuditProposals(store: ScheduledAuditProposalStore, query: ScheduledAuditProposalQuery): MutableList<ScheduledAuditProposal> {
    return query.apply(store.list())
}
