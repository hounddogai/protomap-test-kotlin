package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop proposals by code. */
interface DropProposalStore {
    fun find(code: String): DropProposal?
    fun save(item: DropProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropProposalStore(
    var items: MutableMap<String, DropProposal>,
    var order: MutableList<String>,
) : DropProposalStore {
    override fun find(code: String): DropProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropProposal) {
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

    override fun list(): MutableList<DropProposal> {
        val out = mutableListOf<DropProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropProposalStore(): DropProposalStore {
    return MemoryDropProposalStore(mutableMapOf<String, DropProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropProposalStore(
    var inner: DropProposalStore,
    var cache: TtlCache<DropProposal>,
    var hits: Int,
    var misses: Int,
) : DropProposalStore {
    override fun find(code: String): DropProposal? {
        val hit = (cache.get(code) as DropProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropProposal> = inner.list()
}

fun newCachedDropProposalStore(inner: DropProposalStore, ttl: Int, clock: Clock): DropProposalStore {
    return CachedDropProposalStore(
        inner = inner,
        cache = newTtlCache<DropProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop proposals. Empty fields match all. */
data class DropProposalQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropProposal): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropProposal>): MutableList<DropProposal> {
        val matched = filter(items) { candidate: DropProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropProposals(store: DropProposalStore, query: DropProposalQuery): MutableList<DropProposal> {
    return query.apply(store.list())
}
