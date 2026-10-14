package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub proposals by code. */
interface HubProposalStore {
    fun find(code: String): HubProposal?
    fun save(item: HubProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubProposalStore(
    var items: MutableMap<String, HubProposal>,
    var order: MutableList<String>,
) : HubProposalStore {
    override fun find(code: String): HubProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubProposal) {
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

    override fun list(): MutableList<HubProposal> {
        val out = mutableListOf<HubProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubProposalStore(): HubProposalStore {
    return MemoryHubProposalStore(mutableMapOf<String, HubProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubProposalStore(
    var inner: HubProposalStore,
    var cache: TtlCache<HubProposal>,
    var hits: Int,
    var misses: Int,
) : HubProposalStore {
    override fun find(code: String): HubProposal? {
        val hit = (cache.get(code) as HubProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubProposal> = inner.list()
}

fun newCachedHubProposalStore(inner: HubProposalStore, ttl: Int, clock: Clock): HubProposalStore {
    return CachedHubProposalStore(
        inner = inner,
        cache = newTtlCache<HubProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub proposals. Empty fields match all. */
data class HubProposalQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubProposal>): MutableList<HubProposal> {
        val matched = filter(items) { candidate: HubProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubProposals(store: HubProposalStore, query: HubProposalQuery): MutableList<HubProposal> {
    return query.apply(store.list())
}
