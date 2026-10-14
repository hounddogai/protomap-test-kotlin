package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization proposals by code. */
interface UtilizationProposalStore {
    fun find(code: String): UtilizationProposal?
    fun save(item: UtilizationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationProposalStore(
    var items: MutableMap<String, UtilizationProposal>,
    var order: MutableList<String>,
) : UtilizationProposalStore {
    override fun find(code: String): UtilizationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationProposal) {
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

    override fun list(): MutableList<UtilizationProposal> {
        val out = mutableListOf<UtilizationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationProposalStore(): UtilizationProposalStore {
    return MemoryUtilizationProposalStore(mutableMapOf<String, UtilizationProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationProposalStore(
    var inner: UtilizationProposalStore,
    var cache: TtlCache<UtilizationProposal>,
    var hits: Int,
    var misses: Int,
) : UtilizationProposalStore {
    override fun find(code: String): UtilizationProposal? {
        val hit = (cache.get(code) as UtilizationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationProposal> = inner.list()
}

fun newCachedUtilizationProposalStore(inner: UtilizationProposalStore, ttl: Int, clock: Clock): UtilizationProposalStore {
    return CachedUtilizationProposalStore(
        inner = inner,
        cache = newTtlCache<UtilizationProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization proposals. Empty fields match all. */
data class UtilizationProposalQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationProposal>): MutableList<UtilizationProposal> {
        val matched = filter(items) { candidate: UtilizationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationProposals(store: UtilizationProposalStore, query: UtilizationProposalQuery): MutableList<UtilizationProposal> {
    return query.apply(store.list())
}
