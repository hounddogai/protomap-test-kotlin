package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient capacity proposals by code. */
interface LenientCapacityProposalStore {
    fun find(code: String): LenientCapacityProposal?
    fun save(item: LenientCapacityProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCapacityProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCapacityProposalStore(
    var items: MutableMap<String, LenientCapacityProposal>,
    var order: MutableList<String>,
) : LenientCapacityProposalStore {
    override fun find(code: String): LenientCapacityProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCapacityProposal) {
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

    override fun list(): MutableList<LenientCapacityProposal> {
        val out = mutableListOf<LenientCapacityProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCapacityProposalStore(): LenientCapacityProposalStore {
    return MemoryLenientCapacityProposalStore(
        mutableMapOf<String, LenientCapacityProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCapacityProposalStore(
    var inner: LenientCapacityProposalStore,
    var cache: TtlCache<LenientCapacityProposal>,
    var hits: Int,
    var misses: Int,
) : LenientCapacityProposalStore {
    override fun find(code: String): LenientCapacityProposal? {
        val hit = (cache.get(code) as LenientCapacityProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCapacityProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCapacityProposal> = inner.list()
}

fun newCachedLenientCapacityProposalStore(inner: LenientCapacityProposalStore, ttl: Int, clock: Clock): LenientCapacityProposalStore {
    return CachedLenientCapacityProposalStore(
        inner = inner,
        cache = newTtlCache<LenientCapacityProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient capacity proposals. Empty fields match all. */
data class LenientCapacityProposalQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCapacityProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCapacityProposal>): MutableList<LenientCapacityProposal> {
        val matched = filter(items) { candidate: LenientCapacityProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCapacityProposals(store: LenientCapacityProposalStore, query: LenientCapacityProposalQuery): MutableList<LenientCapacityProposal> {
    return query.apply(store.list())
}
