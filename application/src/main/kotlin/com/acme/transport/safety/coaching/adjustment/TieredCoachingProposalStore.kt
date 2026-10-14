package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered coaching proposals by code. */
interface TieredCoachingProposalStore {
    fun find(code: String): TieredCoachingProposal?
    fun save(item: TieredCoachingProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCoachingProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCoachingProposalStore(
    var items: MutableMap<String, TieredCoachingProposal>,
    var order: MutableList<String>,
) : TieredCoachingProposalStore {
    override fun find(code: String): TieredCoachingProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCoachingProposal) {
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

    override fun list(): MutableList<TieredCoachingProposal> {
        val out = mutableListOf<TieredCoachingProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCoachingProposalStore(): TieredCoachingProposalStore {
    return MemoryTieredCoachingProposalStore(
        mutableMapOf<String, TieredCoachingProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCoachingProposalStore(
    var inner: TieredCoachingProposalStore,
    var cache: TtlCache<TieredCoachingProposal>,
    var hits: Int,
    var misses: Int,
) : TieredCoachingProposalStore {
    override fun find(code: String): TieredCoachingProposal? {
        val hit = (cache.get(code) as TieredCoachingProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCoachingProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCoachingProposal> = inner.list()
}

fun newCachedTieredCoachingProposalStore(inner: TieredCoachingProposalStore, ttl: Int, clock: Clock): TieredCoachingProposalStore {
    return CachedTieredCoachingProposalStore(
        inner = inner,
        cache = newTtlCache<TieredCoachingProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered coaching proposals. Empty fields match all. */
data class TieredCoachingProposalQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCoachingProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCoachingProposal>): MutableList<TieredCoachingProposal> {
        val matched = filter(items) { candidate: TieredCoachingProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCoachingProposals(store: TieredCoachingProposalStore, query: TieredCoachingProposalQuery): MutableList<TieredCoachingProposal> {
    return query.apply(store.list())
}
