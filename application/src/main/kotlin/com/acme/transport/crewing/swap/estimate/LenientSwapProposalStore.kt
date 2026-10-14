package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient swap proposals by code. */
interface LenientSwapProposalStore {
    fun find(code: String): LenientSwapProposal?
    fun save(item: LenientSwapProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientSwapProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientSwapProposalStore(
    var items: MutableMap<String, LenientSwapProposal>,
    var order: MutableList<String>,
) : LenientSwapProposalStore {
    override fun find(code: String): LenientSwapProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientSwapProposal) {
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

    override fun list(): MutableList<LenientSwapProposal> {
        val out = mutableListOf<LenientSwapProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientSwapProposalStore(): LenientSwapProposalStore {
    return MemoryLenientSwapProposalStore(mutableMapOf<String, LenientSwapProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientSwapProposalStore(
    var inner: LenientSwapProposalStore,
    var cache: TtlCache<LenientSwapProposal>,
    var hits: Int,
    var misses: Int,
) : LenientSwapProposalStore {
    override fun find(code: String): LenientSwapProposal? {
        val hit = (cache.get(code) as LenientSwapProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientSwapProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientSwapProposal> = inner.list()
}

fun newCachedLenientSwapProposalStore(inner: LenientSwapProposalStore, ttl: Int, clock: Clock): LenientSwapProposalStore {
    return CachedLenientSwapProposalStore(
        inner = inner,
        cache = newTtlCache<LenientSwapProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient swap proposals. Empty fields match all. */
data class LenientSwapProposalQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientSwapProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientSwapProposal>): MutableList<LenientSwapProposal> {
        val matched = filter(items) { candidate: LenientSwapProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientSwapProposals(store: LenientSwapProposalStore, query: LenientSwapProposalQuery): MutableList<LenientSwapProposal> {
    return query.apply(store.list())
}
