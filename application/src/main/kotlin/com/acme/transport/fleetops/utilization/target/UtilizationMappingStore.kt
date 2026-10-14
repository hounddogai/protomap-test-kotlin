package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization mappings by code. */
interface UtilizationMappingStore {
    fun find(code: String): UtilizationMapping?
    fun save(item: UtilizationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationMappingStore(
    var items: MutableMap<String, UtilizationMapping>,
    var order: MutableList<String>,
) : UtilizationMappingStore {
    override fun find(code: String): UtilizationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationMapping) {
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

    override fun list(): MutableList<UtilizationMapping> {
        val out = mutableListOf<UtilizationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationMappingStore(): UtilizationMappingStore {
    return MemoryUtilizationMappingStore(mutableMapOf<String, UtilizationMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationMappingStore(
    var inner: UtilizationMappingStore,
    var cache: TtlCache<UtilizationMapping>,
    var hits: Int,
    var misses: Int,
) : UtilizationMappingStore {
    override fun find(code: String): UtilizationMapping? {
        val hit = (cache.get(code) as UtilizationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationMapping> = inner.list()
}

fun newCachedUtilizationMappingStore(inner: UtilizationMappingStore, ttl: Int, clock: Clock): UtilizationMappingStore {
    return CachedUtilizationMappingStore(
        inner = inner,
        cache = newTtlCache<UtilizationMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization mappings. Empty fields match all. */
data class UtilizationMappingQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationMapping): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationMapping>): MutableList<UtilizationMapping> {
        val matched = filter(items) { candidate: UtilizationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationMappings(store: UtilizationMappingStore, query: UtilizationMappingQuery): MutableList<UtilizationMapping> {
    return query.apply(store.list())
}
