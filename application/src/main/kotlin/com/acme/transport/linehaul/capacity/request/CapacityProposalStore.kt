package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity proposals by code. */
interface CapacityProposalStore {
    fun find(code: String): CapacityProposal?
    fun save(item: CapacityProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityProposalStore(
    var items: MutableMap<String, CapacityProposal>,
    var order: MutableList<String>,
) : CapacityProposalStore {
    override fun find(code: String): CapacityProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityProposal) {
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

    override fun list(): MutableList<CapacityProposal> {
        val out = mutableListOf<CapacityProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityProposalStore(): CapacityProposalStore {
    return MemoryCapacityProposalStore(mutableMapOf<String, CapacityProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityProposalStore(
    var inner: CapacityProposalStore,
    var cache: TtlCache<CapacityProposal>,
    var hits: Int,
    var misses: Int,
) : CapacityProposalStore {
    override fun find(code: String): CapacityProposal? {
        val hit = (cache.get(code) as CapacityProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityProposal> = inner.list()
}

fun newCachedCapacityProposalStore(inner: CapacityProposalStore, ttl: Int, clock: Clock): CapacityProposalStore {
    return CachedCapacityProposalStore(
        inner = inner,
        cache = newTtlCache<CapacityProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity proposals. Empty fields match all. */
data class CapacityProposalQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityProposal>): MutableList<CapacityProposal> {
        val matched = filter(items) { candidate: CapacityProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityProposals(store: CapacityProposalStore, query: CapacityProposalQuery): MutableList<CapacityProposal> {
    return query.apply(store.list())
}
