package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan proposals by code. */
interface ServicePlanProposalStore {
    fun find(code: String): ServicePlanProposal?
    fun save(item: ServicePlanProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanProposalStore(
    var items: MutableMap<String, ServicePlanProposal>,
    var order: MutableList<String>,
) : ServicePlanProposalStore {
    override fun find(code: String): ServicePlanProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanProposal) {
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

    override fun list(): MutableList<ServicePlanProposal> {
        val out = mutableListOf<ServicePlanProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanProposalStore(): ServicePlanProposalStore {
    return MemoryServicePlanProposalStore(mutableMapOf<String, ServicePlanProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanProposalStore(
    var inner: ServicePlanProposalStore,
    var cache: TtlCache<ServicePlanProposal>,
    var hits: Int,
    var misses: Int,
) : ServicePlanProposalStore {
    override fun find(code: String): ServicePlanProposal? {
        val hit = (cache.get(code) as ServicePlanProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanProposal> = inner.list()
}

fun newCachedServicePlanProposalStore(inner: ServicePlanProposalStore, ttl: Int, clock: Clock): ServicePlanProposalStore {
    return CachedServicePlanProposalStore(
        inner = inner,
        cache = newTtlCache<ServicePlanProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan proposals. Empty fields match all. */
data class ServicePlanProposalQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanProposal>): MutableList<ServicePlanProposal> {
        val matched = filter(items) { candidate: ServicePlanProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanProposals(store: ServicePlanProposalStore, query: ServicePlanProposalQuery): MutableList<ServicePlanProposal> {
    return query.apply(store.list())
}
