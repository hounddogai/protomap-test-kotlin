package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery proposals by code. */
interface RedeliveryProposalStore {
    fun find(code: String): RedeliveryProposal?
    fun save(item: RedeliveryProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryProposalStore(
    var items: MutableMap<String, RedeliveryProposal>,
    var order: MutableList<String>,
) : RedeliveryProposalStore {
    override fun find(code: String): RedeliveryProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryProposal) {
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

    override fun list(): MutableList<RedeliveryProposal> {
        val out = mutableListOf<RedeliveryProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryProposalStore(): RedeliveryProposalStore {
    return MemoryRedeliveryProposalStore(mutableMapOf<String, RedeliveryProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryProposalStore(
    var inner: RedeliveryProposalStore,
    var cache: TtlCache<RedeliveryProposal>,
    var hits: Int,
    var misses: Int,
) : RedeliveryProposalStore {
    override fun find(code: String): RedeliveryProposal? {
        val hit = (cache.get(code) as RedeliveryProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryProposal> = inner.list()
}

fun newCachedRedeliveryProposalStore(inner: RedeliveryProposalStore, ttl: Int, clock: Clock): RedeliveryProposalStore {
    return CachedRedeliveryProposalStore(
        inner = inner,
        cache = newTtlCache<RedeliveryProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery proposals. Empty fields match all. */
data class RedeliveryProposalQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryProposal>): MutableList<RedeliveryProposal> {
        val matched = filter(items) { candidate: RedeliveryProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryProposals(store: RedeliveryProposalStore, query: RedeliveryProposalQuery): MutableList<RedeliveryProposal> {
    return query.apply(store.list())
}
