package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker proposals by code. */
interface AdaptiveLockerProposalStore {
    fun find(code: String): AdaptiveLockerProposal?
    fun save(item: AdaptiveLockerProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerProposalStore(
    var items: MutableMap<String, AdaptiveLockerProposal>,
    var order: MutableList<String>,
) : AdaptiveLockerProposalStore {
    override fun find(code: String): AdaptiveLockerProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerProposal) {
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

    override fun list(): MutableList<AdaptiveLockerProposal> {
        val out = mutableListOf<AdaptiveLockerProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerProposalStore(): AdaptiveLockerProposalStore {
    return MemoryAdaptiveLockerProposalStore(
        mutableMapOf<String, AdaptiveLockerProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerProposalStore(
    var inner: AdaptiveLockerProposalStore,
    var cache: TtlCache<AdaptiveLockerProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerProposalStore {
    override fun find(code: String): AdaptiveLockerProposal? {
        val hit = (cache.get(code) as AdaptiveLockerProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerProposal> = inner.list()
}

fun newCachedAdaptiveLockerProposalStore(inner: AdaptiveLockerProposalStore, ttl: Int, clock: Clock): AdaptiveLockerProposalStore {
    return CachedAdaptiveLockerProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker proposals. Empty fields match all. */
data class AdaptiveLockerProposalQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLockerProposal>): MutableList<AdaptiveLockerProposal> {
        val matched = filter(items) { candidate: AdaptiveLockerProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerProposals(store: AdaptiveLockerProposalStore, query: AdaptiveLockerProposalQuery): MutableList<AdaptiveLockerProposal> {
    return query.apply(store.list())
}
