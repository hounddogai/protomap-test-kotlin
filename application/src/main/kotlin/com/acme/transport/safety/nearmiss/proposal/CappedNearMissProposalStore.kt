package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped near miss proposals by code. */
interface CappedNearMissProposalStore {
    fun find(code: String): CappedNearMissProposal?
    fun save(item: CappedNearMissProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNearMissProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNearMissProposalStore(
    var items: MutableMap<String, CappedNearMissProposal>,
    var order: MutableList<String>,
) : CappedNearMissProposalStore {
    override fun find(code: String): CappedNearMissProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNearMissProposal) {
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

    override fun list(): MutableList<CappedNearMissProposal> {
        val out = mutableListOf<CappedNearMissProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNearMissProposalStore(): CappedNearMissProposalStore {
    return MemoryCappedNearMissProposalStore(
        mutableMapOf<String, CappedNearMissProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNearMissProposalStore(
    var inner: CappedNearMissProposalStore,
    var cache: TtlCache<CappedNearMissProposal>,
    var hits: Int,
    var misses: Int,
) : CappedNearMissProposalStore {
    override fun find(code: String): CappedNearMissProposal? {
        val hit = (cache.get(code) as CappedNearMissProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNearMissProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNearMissProposal> = inner.list()
}

fun newCachedCappedNearMissProposalStore(inner: CappedNearMissProposalStore, ttl: Int, clock: Clock): CappedNearMissProposalStore {
    return CachedCappedNearMissProposalStore(
        inner = inner,
        cache = newTtlCache<CappedNearMissProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped near miss proposals. Empty fields match all. */
data class CappedNearMissProposalQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNearMissProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedNearMissProposal>): MutableList<CappedNearMissProposal> {
        val matched = filter(items) { candidate: CappedNearMissProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNearMissProposals(store: CappedNearMissProposalStore, query: CappedNearMissProposalQuery): MutableList<CappedNearMissProposal> {
    return query.apply(store.list())
}
