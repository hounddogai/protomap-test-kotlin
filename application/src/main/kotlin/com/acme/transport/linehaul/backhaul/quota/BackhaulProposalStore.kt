package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul proposals by code. */
interface BackhaulProposalStore {
    fun find(code: String): BackhaulProposal?
    fun save(item: BackhaulProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulProposalStore(
    var items: MutableMap<String, BackhaulProposal>,
    var order: MutableList<String>,
) : BackhaulProposalStore {
    override fun find(code: String): BackhaulProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulProposal) {
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

    override fun list(): MutableList<BackhaulProposal> {
        val out = mutableListOf<BackhaulProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulProposalStore(): BackhaulProposalStore {
    return MemoryBackhaulProposalStore(mutableMapOf<String, BackhaulProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulProposalStore(
    var inner: BackhaulProposalStore,
    var cache: TtlCache<BackhaulProposal>,
    var hits: Int,
    var misses: Int,
) : BackhaulProposalStore {
    override fun find(code: String): BackhaulProposal? {
        val hit = (cache.get(code) as BackhaulProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulProposal> = inner.list()
}

fun newCachedBackhaulProposalStore(inner: BackhaulProposalStore, ttl: Int, clock: Clock): BackhaulProposalStore {
    return CachedBackhaulProposalStore(
        inner = inner,
        cache = newTtlCache<BackhaulProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul proposals. Empty fields match all. */
data class BackhaulProposalQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulProposal>): MutableList<BackhaulProposal> {
        val matched = filter(items) { candidate: BackhaulProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulProposals(store: BackhaulProposalStore, query: BackhaulProposalQuery): MutableList<BackhaulProposal> {
    return query.apply(store.list())
}
