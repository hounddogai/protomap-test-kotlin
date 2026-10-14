package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area claims by code. */
interface ServiceAreaClaimStore {
    fun find(code: String): ServiceAreaClaim?
    fun save(item: ServiceAreaClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaClaimStore(
    var items: MutableMap<String, ServiceAreaClaim>,
    var order: MutableList<String>,
) : ServiceAreaClaimStore {
    override fun find(code: String): ServiceAreaClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaClaim) {
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

    override fun list(): MutableList<ServiceAreaClaim> {
        val out = mutableListOf<ServiceAreaClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaClaimStore(): ServiceAreaClaimStore {
    return MemoryServiceAreaClaimStore(mutableMapOf<String, ServiceAreaClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaClaimStore(
    var inner: ServiceAreaClaimStore,
    var cache: TtlCache<ServiceAreaClaim>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaClaimStore {
    override fun find(code: String): ServiceAreaClaim? {
        val hit = (cache.get(code) as ServiceAreaClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaClaim> = inner.list()
}

fun newCachedServiceAreaClaimStore(inner: ServiceAreaClaimStore, ttl: Int, clock: Clock): ServiceAreaClaimStore {
    return CachedServiceAreaClaimStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area claims. Empty fields match all. */
data class ServiceAreaClaimQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaClaim): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaClaim>): MutableList<ServiceAreaClaim> {
        val matched = filter(items) { candidate: ServiceAreaClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaClaims(store: ServiceAreaClaimStore, query: ServiceAreaClaimQuery): MutableList<ServiceAreaClaim> {
    return query.apply(store.list())
}
