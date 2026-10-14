package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender proposals by code. */
interface TenderProposalStore {
    fun find(code: String): TenderProposal?
    fun save(item: TenderProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderProposalStore(
    var items: MutableMap<String, TenderProposal>,
    var order: MutableList<String>,
) : TenderProposalStore {
    override fun find(code: String): TenderProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderProposal) {
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

    override fun list(): MutableList<TenderProposal> {
        val out = mutableListOf<TenderProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderProposalStore(): TenderProposalStore {
    return MemoryTenderProposalStore(mutableMapOf<String, TenderProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderProposalStore(
    var inner: TenderProposalStore,
    var cache: TtlCache<TenderProposal>,
    var hits: Int,
    var misses: Int,
) : TenderProposalStore {
    override fun find(code: String): TenderProposal? {
        val hit = (cache.get(code) as TenderProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderProposal> = inner.list()
}

fun newCachedTenderProposalStore(inner: TenderProposalStore, ttl: Int, clock: Clock): TenderProposalStore {
    return CachedTenderProposalStore(
        inner = inner,
        cache = newTtlCache<TenderProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender proposals. Empty fields match all. */
data class TenderProposalQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderProposal>): MutableList<TenderProposal> {
        val matched = filter(items) { candidate: TenderProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderProposals(store: TenderProposalStore, query: TenderProposalQuery): MutableList<TenderProposal> {
    return query.apply(store.list())
}
