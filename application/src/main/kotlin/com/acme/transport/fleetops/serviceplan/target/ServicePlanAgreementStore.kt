package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan agreements by code. */
interface ServicePlanAgreementStore {
    fun find(code: String): ServicePlanAgreement?
    fun save(item: ServicePlanAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanAgreementStore(
    var items: MutableMap<String, ServicePlanAgreement>,
    var order: MutableList<String>,
) : ServicePlanAgreementStore {
    override fun find(code: String): ServicePlanAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanAgreement) {
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

    override fun list(): MutableList<ServicePlanAgreement> {
        val out = mutableListOf<ServicePlanAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanAgreementStore(): ServicePlanAgreementStore {
    return MemoryServicePlanAgreementStore(
        mutableMapOf<String, ServicePlanAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanAgreementStore(
    var inner: ServicePlanAgreementStore,
    var cache: TtlCache<ServicePlanAgreement>,
    var hits: Int,
    var misses: Int,
) : ServicePlanAgreementStore {
    override fun find(code: String): ServicePlanAgreement? {
        val hit = (cache.get(code) as ServicePlanAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanAgreement> = inner.list()
}

fun newCachedServicePlanAgreementStore(inner: ServicePlanAgreementStore, ttl: Int, clock: Clock): ServicePlanAgreementStore {
    return CachedServicePlanAgreementStore(
        inner = inner,
        cache = newTtlCache<ServicePlanAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan agreements. Empty fields match all. */
data class ServicePlanAgreementQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanAgreement>): MutableList<ServicePlanAgreement> {
        val matched = filter(items) { candidate: ServicePlanAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanAgreements(store: ServicePlanAgreementStore, query: ServicePlanAgreementQuery): MutableList<ServicePlanAgreement> {
    return query.apply(store.list())
}
