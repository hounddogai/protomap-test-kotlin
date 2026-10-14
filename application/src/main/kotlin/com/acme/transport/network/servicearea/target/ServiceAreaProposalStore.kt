package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area proposals by code. */
interface ServiceAreaProposalStore {
    fun find(code: String): ServiceAreaProposal?
    fun save(item: ServiceAreaProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaProposalStore(
    var items: MutableMap<String, ServiceAreaProposal>,
    var order: MutableList<String>,
) : ServiceAreaProposalStore {
    override fun find(code: String): ServiceAreaProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaProposal) {
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

    override fun list(): MutableList<ServiceAreaProposal> {
        val out = mutableListOf<ServiceAreaProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaProposalStore(): ServiceAreaProposalStore {
    return MemoryServiceAreaProposalStore(mutableMapOf<String, ServiceAreaProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaProposalStore(
    var inner: ServiceAreaProposalStore,
    var cache: TtlCache<ServiceAreaProposal>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaProposalStore {
    override fun find(code: String): ServiceAreaProposal? {
        val hit = (cache.get(code) as ServiceAreaProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaProposal> = inner.list()
}

fun newCachedServiceAreaProposalStore(inner: ServiceAreaProposalStore, ttl: Int, clock: Clock): ServiceAreaProposalStore {
    return CachedServiceAreaProposalStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area proposals. Empty fields match all. */
data class ServiceAreaProposalQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaProposal>): MutableList<ServiceAreaProposal> {
        val matched = filter(items) { candidate: ServiceAreaProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaProposals(store: ServiceAreaProposalStore, query: ServiceAreaProposalQuery): MutableList<ServiceAreaProposal> {
    return query.apply(store.list())
}
