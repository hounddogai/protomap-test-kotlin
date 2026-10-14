package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area digests by code. */
interface ServiceAreaDigestStore {
    fun find(code: String): ServiceAreaDigest?
    fun save(item: ServiceAreaDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaDigestStore(
    var items: MutableMap<String, ServiceAreaDigest>,
    var order: MutableList<String>,
) : ServiceAreaDigestStore {
    override fun find(code: String): ServiceAreaDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaDigest) {
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

    override fun list(): MutableList<ServiceAreaDigest> {
        val out = mutableListOf<ServiceAreaDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaDigestStore(): ServiceAreaDigestStore {
    return MemoryServiceAreaDigestStore(mutableMapOf<String, ServiceAreaDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaDigestStore(
    var inner: ServiceAreaDigestStore,
    var cache: TtlCache<ServiceAreaDigest>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaDigestStore {
    override fun find(code: String): ServiceAreaDigest? {
        val hit = (cache.get(code) as ServiceAreaDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaDigest> = inner.list()
}

fun newCachedServiceAreaDigestStore(inner: ServiceAreaDigestStore, ttl: Int, clock: Clock): ServiceAreaDigestStore {
    return CachedServiceAreaDigestStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area digests. Empty fields match all. */
data class ServiceAreaDigestQuery(
    var locale: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaDigest>): MutableList<ServiceAreaDigest> {
        val matched = filter(items) { candidate: ServiceAreaDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaDigests(store: ServiceAreaDigestStore, query: ServiceAreaDigestQuery): MutableList<ServiceAreaDigest> {
    return query.apply(store.list())
}
