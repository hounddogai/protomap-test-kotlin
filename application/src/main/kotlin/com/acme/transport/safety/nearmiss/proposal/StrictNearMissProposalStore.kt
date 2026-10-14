package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict near miss proposals by code. */
interface StrictNearMissProposalStore {
    fun find(code: String): StrictNearMissProposal?
    fun save(item: StrictNearMissProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNearMissProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNearMissProposalStore(
    var items: MutableMap<String, StrictNearMissProposal>,
    var order: MutableList<String>,
) : StrictNearMissProposalStore {
    override fun find(code: String): StrictNearMissProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNearMissProposal) {
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

    override fun list(): MutableList<StrictNearMissProposal> {
        val out = mutableListOf<StrictNearMissProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNearMissProposalStore(): StrictNearMissProposalStore {
    return MemoryStrictNearMissProposalStore(
        mutableMapOf<String, StrictNearMissProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNearMissProposalStore(
    var inner: StrictNearMissProposalStore,
    var cache: TtlCache<StrictNearMissProposal>,
    var hits: Int,
    var misses: Int,
) : StrictNearMissProposalStore {
    override fun find(code: String): StrictNearMissProposal? {
        val hit = (cache.get(code) as StrictNearMissProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNearMissProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNearMissProposal> = inner.list()
}

fun newCachedStrictNearMissProposalStore(inner: StrictNearMissProposalStore, ttl: Int, clock: Clock): StrictNearMissProposalStore {
    return CachedStrictNearMissProposalStore(
        inner = inner,
        cache = newTtlCache<StrictNearMissProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict near miss proposals. Empty fields match all. */
data class StrictNearMissProposalQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNearMissProposal): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNearMissProposal>): MutableList<StrictNearMissProposal> {
        val matched = filter(items) { candidate: StrictNearMissProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNearMissProposals(store: StrictNearMissProposalStore, query: StrictNearMissProposalQuery): MutableList<StrictNearMissProposal> {
    return query.apply(store.list())
}
