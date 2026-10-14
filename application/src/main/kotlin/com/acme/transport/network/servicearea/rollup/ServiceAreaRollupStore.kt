package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area rollups by code. */
interface ServiceAreaRollupStore {
    fun find(code: String): ServiceAreaRollup?
    fun save(item: ServiceAreaRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaRollupStore(
    var items: MutableMap<String, ServiceAreaRollup>,
    var order: MutableList<String>,
) : ServiceAreaRollupStore {
    override fun find(code: String): ServiceAreaRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaRollup) {
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

    override fun list(): MutableList<ServiceAreaRollup> {
        val out = mutableListOf<ServiceAreaRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaRollupStore(): ServiceAreaRollupStore {
    return MemoryServiceAreaRollupStore(mutableMapOf<String, ServiceAreaRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaRollupStore(
    var inner: ServiceAreaRollupStore,
    var cache: TtlCache<ServiceAreaRollup>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaRollupStore {
    override fun find(code: String): ServiceAreaRollup? {
        val hit = (cache.get(code) as ServiceAreaRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaRollup> = inner.list()
}

fun newCachedServiceAreaRollupStore(inner: ServiceAreaRollupStore, ttl: Int, clock: Clock): ServiceAreaRollupStore {
    return CachedServiceAreaRollupStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area rollups. Empty fields match all. */
data class ServiceAreaRollupQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ServiceAreaRollup>): MutableList<ServiceAreaRollup> {
        val matched = filter(items) { candidate: ServiceAreaRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaRollups(store: ServiceAreaRollupStore, query: ServiceAreaRollupQuery): MutableList<ServiceAreaRollup> {
    return query.apply(store.list())
}
