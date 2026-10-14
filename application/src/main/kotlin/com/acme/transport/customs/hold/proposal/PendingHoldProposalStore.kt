package com.acme.transport.customs.hold.proposal

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending hold proposals by code. */
interface PendingHoldProposalStore {
    fun find(code: String): PendingHoldProposal?
    fun save(item: PendingHoldProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHoldProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHoldProposalStore(
    var items: MutableMap<String, PendingHoldProposal>,
    var order: MutableList<String>,
) : PendingHoldProposalStore {
    override fun find(code: String): PendingHoldProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHoldProposal) {
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

    override fun list(): MutableList<PendingHoldProposal> {
        val out = mutableListOf<PendingHoldProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHoldProposalStore(): PendingHoldProposalStore {
    return MemoryPendingHoldProposalStore(mutableMapOf<String, PendingHoldProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHoldProposalStore(
    var inner: PendingHoldProposalStore,
    var cache: TtlCache<PendingHoldProposal>,
    var hits: Int,
    var misses: Int,
) : PendingHoldProposalStore {
    override fun find(code: String): PendingHoldProposal? {
        val hit = (cache.get(code) as PendingHoldProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHoldProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHoldProposal> = inner.list()
}

fun newCachedPendingHoldProposalStore(inner: PendingHoldProposalStore, ttl: Int, clock: Clock): PendingHoldProposalStore {
    return CachedPendingHoldProposalStore(
        inner = inner,
        cache = newTtlCache<PendingHoldProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hold proposals. Empty fields match all. */
data class PendingHoldProposalQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHoldProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingHoldProposal>): MutableList<PendingHoldProposal> {
        val matched = filter(items) { candidate: PendingHoldProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHoldProposals(store: PendingHoldProposalStore, query: PendingHoldProposalQuery): MutableList<PendingHoldProposal> {
    return query.apply(store.list())
}
