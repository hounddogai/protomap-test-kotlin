package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict hub proposals by code. */
interface StrictHubProposalStore {
    fun find(code: String): StrictHubProposal?
    fun save(item: StrictHubProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictHubProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictHubProposalStore(
    var items: MutableMap<String, StrictHubProposal>,
    var order: MutableList<String>,
) : StrictHubProposalStore {
    override fun find(code: String): StrictHubProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictHubProposal) {
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

    override fun list(): MutableList<StrictHubProposal> {
        val out = mutableListOf<StrictHubProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictHubProposalStore(): StrictHubProposalStore {
    return MemoryStrictHubProposalStore(mutableMapOf<String, StrictHubProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictHubProposalStore(
    var inner: StrictHubProposalStore,
    var cache: TtlCache<StrictHubProposal>,
    var hits: Int,
    var misses: Int,
) : StrictHubProposalStore {
    override fun find(code: String): StrictHubProposal? {
        val hit = (cache.get(code) as StrictHubProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictHubProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictHubProposal> = inner.list()
}

fun newCachedStrictHubProposalStore(inner: StrictHubProposalStore, ttl: Int, clock: Clock): StrictHubProposalStore {
    return CachedStrictHubProposalStore(
        inner = inner,
        cache = newTtlCache<StrictHubProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict hub proposals. Empty fields match all. */
data class StrictHubProposalQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictHubProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictHubProposal>): MutableList<StrictHubProposal> {
        val matched = filter(items) { candidate: StrictHubProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictHubProposals(store: StrictHubProposalStore, query: StrictHubProposalQuery): MutableList<StrictHubProposal> {
    return query.apply(store.list())
}
