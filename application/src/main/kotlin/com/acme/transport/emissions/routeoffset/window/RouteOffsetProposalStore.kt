package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset proposals by code. */
interface RouteOffsetProposalStore {
    fun find(code: String): RouteOffsetProposal?
    fun save(item: RouteOffsetProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetProposalStore(
    var items: MutableMap<String, RouteOffsetProposal>,
    var order: MutableList<String>,
) : RouteOffsetProposalStore {
    override fun find(code: String): RouteOffsetProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetProposal) {
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

    override fun list(): MutableList<RouteOffsetProposal> {
        val out = mutableListOf<RouteOffsetProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetProposalStore(): RouteOffsetProposalStore {
    return MemoryRouteOffsetProposalStore(mutableMapOf<String, RouteOffsetProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetProposalStore(
    var inner: RouteOffsetProposalStore,
    var cache: TtlCache<RouteOffsetProposal>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetProposalStore {
    override fun find(code: String): RouteOffsetProposal? {
        val hit = (cache.get(code) as RouteOffsetProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetProposal> = inner.list()
}

fun newCachedRouteOffsetProposalStore(inner: RouteOffsetProposalStore, ttl: Int, clock: Clock): RouteOffsetProposalStore {
    return CachedRouteOffsetProposalStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset proposals. Empty fields match all. */
data class RouteOffsetProposalQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetProposal>): MutableList<RouteOffsetProposal> {
        val matched = filter(items) { candidate: RouteOffsetProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetProposals(store: RouteOffsetProposalStore, query: RouteOffsetProposalQuery): MutableList<RouteOffsetProposal> {
    return query.apply(store.list())
}
