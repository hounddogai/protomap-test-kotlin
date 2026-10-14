package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area agreements by code. */
interface ServiceAreaAgreementStore {
    fun find(code: String): ServiceAreaAgreement?
    fun save(item: ServiceAreaAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaAgreementStore(
    var items: MutableMap<String, ServiceAreaAgreement>,
    var order: MutableList<String>,
) : ServiceAreaAgreementStore {
    override fun find(code: String): ServiceAreaAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaAgreement) {
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

    override fun list(): MutableList<ServiceAreaAgreement> {
        val out = mutableListOf<ServiceAreaAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaAgreementStore(): ServiceAreaAgreementStore {
    return MemoryServiceAreaAgreementStore(
        mutableMapOf<String, ServiceAreaAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaAgreementStore(
    var inner: ServiceAreaAgreementStore,
    var cache: TtlCache<ServiceAreaAgreement>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaAgreementStore {
    override fun find(code: String): ServiceAreaAgreement? {
        val hit = (cache.get(code) as ServiceAreaAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaAgreement> = inner.list()
}

fun newCachedServiceAreaAgreementStore(inner: ServiceAreaAgreementStore, ttl: Int, clock: Clock): ServiceAreaAgreementStore {
    return CachedServiceAreaAgreementStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area agreements. Empty fields match all. */
data class ServiceAreaAgreementQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaAgreement>): MutableList<ServiceAreaAgreement> {
        val matched = filter(items) { candidate: ServiceAreaAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaAgreements(store: ServiceAreaAgreementStore, query: ServiceAreaAgreementQuery): MutableList<ServiceAreaAgreement> {
    return query.apply(store.list())
}
