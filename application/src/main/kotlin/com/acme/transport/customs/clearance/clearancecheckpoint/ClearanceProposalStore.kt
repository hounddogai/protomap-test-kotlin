package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance proposals by code. */
interface ClearanceProposalStore {
    fun find(code: String): ClearanceProposal?
    fun save(item: ClearanceProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceProposalStore(
    var items: MutableMap<String, ClearanceProposal>,
    var order: MutableList<String>,
) : ClearanceProposalStore {
    override fun find(code: String): ClearanceProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceProposal) {
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

    override fun list(): MutableList<ClearanceProposal> {
        val out = mutableListOf<ClearanceProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceProposalStore(): ClearanceProposalStore {
    return MemoryClearanceProposalStore(mutableMapOf<String, ClearanceProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceProposalStore(
    var inner: ClearanceProposalStore,
    var cache: TtlCache<ClearanceProposal>,
    var hits: Int,
    var misses: Int,
) : ClearanceProposalStore {
    override fun find(code: String): ClearanceProposal? {
        val hit = (cache.get(code) as ClearanceProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceProposal> = inner.list()
}

fun newCachedClearanceProposalStore(inner: ClearanceProposalStore, ttl: Int, clock: Clock): ClearanceProposalStore {
    return CachedClearanceProposalStore(
        inner = inner,
        cache = newTtlCache<ClearanceProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance proposals. Empty fields match all. */
data class ClearanceProposalQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceProposal>): MutableList<ClearanceProposal> {
        val matched = filter(items) { candidate: ClearanceProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceProposals(store: ClearanceProposalStore, query: ClearanceProposalQuery): MutableList<ClearanceProposal> {
    return query.apply(store.list())
}
