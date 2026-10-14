package com.acme.transport.network.node.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered node proposals by code. */
interface TieredNodeProposalStore {
    fun find(code: String): TieredNodeProposal?
    fun save(item: TieredNodeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredNodeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredNodeProposalStore(
    var items: MutableMap<String, TieredNodeProposal>,
    var order: MutableList<String>,
) : TieredNodeProposalStore {
    override fun find(code: String): TieredNodeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredNodeProposal) {
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

    override fun list(): MutableList<TieredNodeProposal> {
        val out = mutableListOf<TieredNodeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredNodeProposalStore(): TieredNodeProposalStore {
    return MemoryTieredNodeProposalStore(mutableMapOf<String, TieredNodeProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredNodeProposalStore(
    var inner: TieredNodeProposalStore,
    var cache: TtlCache<TieredNodeProposal>,
    var hits: Int,
    var misses: Int,
) : TieredNodeProposalStore {
    override fun find(code: String): TieredNodeProposal? {
        val hit = (cache.get(code) as TieredNodeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredNodeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredNodeProposal> = inner.list()
}

fun newCachedTieredNodeProposalStore(inner: TieredNodeProposalStore, ttl: Int, clock: Clock): TieredNodeProposalStore {
    return CachedTieredNodeProposalStore(
        inner = inner,
        cache = newTtlCache<TieredNodeProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered node proposals. Empty fields match all. */
data class TieredNodeProposalQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredNodeProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredNodeProposal>): MutableList<TieredNodeProposal> {
        val matched = filter(items) { candidate: TieredNodeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredNodeProposals(store: TieredNodeProposalStore, query: TieredNodeProposalQuery): MutableList<TieredNodeProposal> {
    return query.apply(store.list())
}
