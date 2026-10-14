package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient parcel proposals by code. */
interface LenientParcelProposalStore {
    fun find(code: String): LenientParcelProposal?
    fun save(item: LenientParcelProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientParcelProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientParcelProposalStore(
    var items: MutableMap<String, LenientParcelProposal>,
    var order: MutableList<String>,
) : LenientParcelProposalStore {
    override fun find(code: String): LenientParcelProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientParcelProposal) {
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

    override fun list(): MutableList<LenientParcelProposal> {
        val out = mutableListOf<LenientParcelProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientParcelProposalStore(): LenientParcelProposalStore {
    return MemoryLenientParcelProposalStore(
        mutableMapOf<String, LenientParcelProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientParcelProposalStore(
    var inner: LenientParcelProposalStore,
    var cache: TtlCache<LenientParcelProposal>,
    var hits: Int,
    var misses: Int,
) : LenientParcelProposalStore {
    override fun find(code: String): LenientParcelProposal? {
        val hit = (cache.get(code) as LenientParcelProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientParcelProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientParcelProposal> = inner.list()
}

fun newCachedLenientParcelProposalStore(inner: LenientParcelProposalStore, ttl: Int, clock: Clock): LenientParcelProposalStore {
    return CachedLenientParcelProposalStore(
        inner = inner,
        cache = newTtlCache<LenientParcelProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient parcel proposals. Empty fields match all. */
data class LenientParcelProposalQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientParcelProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientParcelProposal>): MutableList<LenientParcelProposal> {
        val matched = filter(items) { candidate: LenientParcelProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientParcelProposals(store: LenientParcelProposalStore, query: LenientParcelProposalQuery): MutableList<LenientParcelProposal> {
    return query.apply(store.list())
}
