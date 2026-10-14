package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset mappings by code. */
interface ScheduledRouteOffsetMappingStore {
    fun find(code: String): ScheduledRouteOffsetMapping?
    fun save(item: ScheduledRouteOffsetMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetMappingStore(
    var items: MutableMap<String, ScheduledRouteOffsetMapping>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetMappingStore {
    override fun find(code: String): ScheduledRouteOffsetMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetMapping) {
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

    override fun list(): MutableList<ScheduledRouteOffsetMapping> {
        val out = mutableListOf<ScheduledRouteOffsetMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetMappingStore(): ScheduledRouteOffsetMappingStore {
    return MemoryScheduledRouteOffsetMappingStore(
        mutableMapOf<String, ScheduledRouteOffsetMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetMappingStore(
    var inner: ScheduledRouteOffsetMappingStore,
    var cache: TtlCache<ScheduledRouteOffsetMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetMappingStore {
    override fun find(code: String): ScheduledRouteOffsetMapping? {
        val hit = (cache.get(code) as ScheduledRouteOffsetMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetMapping> = inner.list()
}

fun newCachedScheduledRouteOffsetMappingStore(inner: ScheduledRouteOffsetMappingStore, ttl: Int, clock: Clock): ScheduledRouteOffsetMappingStore {
    return CachedScheduledRouteOffsetMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset mappings. Empty fields match all. */
data class ScheduledRouteOffsetMappingQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetMapping): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRouteOffsetMapping>): MutableList<ScheduledRouteOffsetMapping> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetMappings(store: ScheduledRouteOffsetMappingStore, query: ScheduledRouteOffsetMappingQuery): MutableList<ScheduledRouteOffsetMapping> {
    return query.apply(store.list())
}
