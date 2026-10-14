package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer proposals by code. */
interface TrailerProposalStore {
    fun find(code: String): TrailerProposal?
    fun save(item: TrailerProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerProposalStore(
    var items: MutableMap<String, TrailerProposal>,
    var order: MutableList<String>,
) : TrailerProposalStore {
    override fun find(code: String): TrailerProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerProposal) {
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

    override fun list(): MutableList<TrailerProposal> {
        val out = mutableListOf<TrailerProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerProposalStore(): TrailerProposalStore {
    return MemoryTrailerProposalStore(mutableMapOf<String, TrailerProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerProposalStore(
    var inner: TrailerProposalStore,
    var cache: TtlCache<TrailerProposal>,
    var hits: Int,
    var misses: Int,
) : TrailerProposalStore {
    override fun find(code: String): TrailerProposal? {
        val hit = (cache.get(code) as TrailerProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerProposal> = inner.list()
}

fun newCachedTrailerProposalStore(inner: TrailerProposalStore, ttl: Int, clock: Clock): TrailerProposalStore {
    return CachedTrailerProposalStore(
        inner = inner,
        cache = newTtlCache<TrailerProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer proposals. Empty fields match all. */
data class TrailerProposalQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerProposal): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TrailerProposal>): MutableList<TrailerProposal> {
        val matched = filter(items) { candidate: TrailerProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerProposals(store: TrailerProposalStore, query: TrailerProposalQuery): MutableList<TrailerProposal> {
    return query.apply(store.list())
}
