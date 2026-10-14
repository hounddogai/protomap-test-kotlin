package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy hub proposals by code. */
interface LegacyHubProposalStore {
    fun find(code: String): LegacyHubProposal?
    fun save(item: LegacyHubProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyHubProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyHubProposalStore(
    var items: MutableMap<String, LegacyHubProposal>,
    var order: MutableList<String>,
) : LegacyHubProposalStore {
    override fun find(code: String): LegacyHubProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyHubProposal) {
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

    override fun list(): MutableList<LegacyHubProposal> {
        val out = mutableListOf<LegacyHubProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyHubProposalStore(): LegacyHubProposalStore {
    return MemoryLegacyHubProposalStore(mutableMapOf<String, LegacyHubProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyHubProposalStore(
    var inner: LegacyHubProposalStore,
    var cache: TtlCache<LegacyHubProposal>,
    var hits: Int,
    var misses: Int,
) : LegacyHubProposalStore {
    override fun find(code: String): LegacyHubProposal? {
        val hit = (cache.get(code) as LegacyHubProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyHubProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyHubProposal> = inner.list()
}

fun newCachedLegacyHubProposalStore(inner: LegacyHubProposalStore, ttl: Int, clock: Clock): LegacyHubProposalStore {
    return CachedLegacyHubProposalStore(
        inner = inner,
        cache = newTtlCache<LegacyHubProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy hub proposals. Empty fields match all. */
data class LegacyHubProposalQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyHubProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyHubProposal>): MutableList<LegacyHubProposal> {
        val matched = filter(items) { candidate: LegacyHubProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyHubProposals(store: LegacyHubProposalStore, query: LegacyHubProposalQuery): MutableList<LegacyHubProposal> {
    return query.apply(store.list())
}
