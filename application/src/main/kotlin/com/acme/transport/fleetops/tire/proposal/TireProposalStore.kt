package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire proposals by code. */
interface TireProposalStore {
    fun find(code: String): TireProposal?
    fun save(item: TireProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireProposalStore(
    var items: MutableMap<String, TireProposal>,
    var order: MutableList<String>,
) : TireProposalStore {
    override fun find(code: String): TireProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireProposal) {
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

    override fun list(): MutableList<TireProposal> {
        val out = mutableListOf<TireProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireProposalStore(): TireProposalStore {
    return MemoryTireProposalStore(mutableMapOf<String, TireProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireProposalStore(
    var inner: TireProposalStore,
    var cache: TtlCache<TireProposal>,
    var hits: Int,
    var misses: Int,
) : TireProposalStore {
    override fun find(code: String): TireProposal? {
        val hit = (cache.get(code) as TireProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireProposal> = inner.list()
}

fun newCachedTireProposalStore(inner: TireProposalStore, ttl: Int, clock: Clock): TireProposalStore {
    return CachedTireProposalStore(
        inner = inner,
        cache = newTtlCache<TireProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire proposals. Empty fields match all. */
data class TireProposalQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireProposal>): MutableList<TireProposal> {
        val matched = filter(items) { candidate: TireProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireProposals(store: TireProposalStore, query: TireProposalQuery): MutableList<TireProposal> {
    return query.apply(store.list())
}
