package com.acme.transport.fleetops.inspection.group

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection proposals by code. */
interface InspectionProposalStore {
    fun find(code: String): InspectionProposal?
    fun save(item: InspectionProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionProposalStore(
    var items: MutableMap<String, InspectionProposal>,
    var order: MutableList<String>,
) : InspectionProposalStore {
    override fun find(code: String): InspectionProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionProposal) {
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

    override fun list(): MutableList<InspectionProposal> {
        val out = mutableListOf<InspectionProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionProposalStore(): InspectionProposalStore {
    return MemoryInspectionProposalStore(mutableMapOf<String, InspectionProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionProposalStore(
    var inner: InspectionProposalStore,
    var cache: TtlCache<InspectionProposal>,
    var hits: Int,
    var misses: Int,
) : InspectionProposalStore {
    override fun find(code: String): InspectionProposal? {
        val hit = (cache.get(code) as InspectionProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionProposal> = inner.list()
}

fun newCachedInspectionProposalStore(inner: InspectionProposalStore, ttl: Int, clock: Clock): InspectionProposalStore {
    return CachedInspectionProposalStore(
        inner = inner,
        cache = newTtlCache<InspectionProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection proposals. Empty fields match all. */
data class InspectionProposalQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionProposal>): MutableList<InspectionProposal> {
        val matched = filter(items) { candidate: InspectionProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionProposals(store: InspectionProposalStore, query: InspectionProposalQuery): MutableList<InspectionProposal> {
    return query.apply(store.list())
}
