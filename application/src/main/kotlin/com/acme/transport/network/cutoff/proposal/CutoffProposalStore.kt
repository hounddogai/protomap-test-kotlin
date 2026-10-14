package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff proposals by code. */
interface CutoffProposalStore {
    fun find(code: String): CutoffProposal?
    fun save(item: CutoffProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffProposalStore(
    var items: MutableMap<String, CutoffProposal>,
    var order: MutableList<String>,
) : CutoffProposalStore {
    override fun find(code: String): CutoffProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffProposal) {
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

    override fun list(): MutableList<CutoffProposal> {
        val out = mutableListOf<CutoffProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffProposalStore(): CutoffProposalStore {
    return MemoryCutoffProposalStore(mutableMapOf<String, CutoffProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffProposalStore(
    var inner: CutoffProposalStore,
    var cache: TtlCache<CutoffProposal>,
    var hits: Int,
    var misses: Int,
) : CutoffProposalStore {
    override fun find(code: String): CutoffProposal? {
        val hit = (cache.get(code) as CutoffProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffProposal> = inner.list()
}

fun newCachedCutoffProposalStore(inner: CutoffProposalStore, ttl: Int, clock: Clock): CutoffProposalStore {
    return CachedCutoffProposalStore(
        inner = inner,
        cache = newTtlCache<CutoffProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff proposals. Empty fields match all. */
data class CutoffProposalQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffProposal>): MutableList<CutoffProposal> {
        val matched = filter(items) { candidate: CutoffProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffProposals(store: CutoffProposalStore, query: CutoffProposalQuery): MutableList<CutoffProposal> {
    return query.apply(store.list())
}
