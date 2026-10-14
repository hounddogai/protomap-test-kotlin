package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area overrides by code. */
interface ServiceAreaOverrideStore {
    fun find(code: String): ServiceAreaOverride?
    fun save(item: ServiceAreaOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaOverrideStore(
    var items: MutableMap<String, ServiceAreaOverride>,
    var order: MutableList<String>,
) : ServiceAreaOverrideStore {
    override fun find(code: String): ServiceAreaOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaOverride) {
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

    override fun list(): MutableList<ServiceAreaOverride> {
        val out = mutableListOf<ServiceAreaOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaOverrideStore(): ServiceAreaOverrideStore {
    return MemoryServiceAreaOverrideStore(mutableMapOf<String, ServiceAreaOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaOverrideStore(
    var inner: ServiceAreaOverrideStore,
    var cache: TtlCache<ServiceAreaOverride>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaOverrideStore {
    override fun find(code: String): ServiceAreaOverride? {
        val hit = (cache.get(code) as ServiceAreaOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaOverride> = inner.list()
}

fun newCachedServiceAreaOverrideStore(inner: ServiceAreaOverrideStore, ttl: Int, clock: Clock): ServiceAreaOverrideStore {
    return CachedServiceAreaOverrideStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area overrides. Empty fields match all. */
data class ServiceAreaOverrideQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaOverride>): MutableList<ServiceAreaOverride> {
        val matched = filter(items) { candidate: ServiceAreaOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaOverrides(store: ServiceAreaOverrideStore, query: ServiceAreaOverrideQuery): MutableList<ServiceAreaOverride> {
    return query.apply(store.list())
}
