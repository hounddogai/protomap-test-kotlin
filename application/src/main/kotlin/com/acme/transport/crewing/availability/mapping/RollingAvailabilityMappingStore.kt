package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling availability mappings by code. */
interface RollingAvailabilityMappingStore {
    fun find(code: String): RollingAvailabilityMapping?
    fun save(item: RollingAvailabilityMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAvailabilityMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAvailabilityMappingStore(
    var items: MutableMap<String, RollingAvailabilityMapping>,
    var order: MutableList<String>,
) : RollingAvailabilityMappingStore {
    override fun find(code: String): RollingAvailabilityMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAvailabilityMapping) {
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

    override fun list(): MutableList<RollingAvailabilityMapping> {
        val out = mutableListOf<RollingAvailabilityMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAvailabilityMappingStore(): RollingAvailabilityMappingStore {
    return MemoryRollingAvailabilityMappingStore(
        mutableMapOf<String, RollingAvailabilityMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAvailabilityMappingStore(
    var inner: RollingAvailabilityMappingStore,
    var cache: TtlCache<RollingAvailabilityMapping>,
    var hits: Int,
    var misses: Int,
) : RollingAvailabilityMappingStore {
    override fun find(code: String): RollingAvailabilityMapping? {
        val hit = (cache.get(code) as RollingAvailabilityMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAvailabilityMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAvailabilityMapping> = inner.list()
}

fun newCachedRollingAvailabilityMappingStore(inner: RollingAvailabilityMappingStore, ttl: Int, clock: Clock): RollingAvailabilityMappingStore {
    return CachedRollingAvailabilityMappingStore(
        inner = inner,
        cache = newTtlCache<RollingAvailabilityMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling availability mappings. Empty fields match all. */
data class RollingAvailabilityMappingQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAvailabilityMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAvailabilityMapping>): MutableList<RollingAvailabilityMapping> {
        val matched = filter(items) { candidate: RollingAvailabilityMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAvailabilityMappings(store: RollingAvailabilityMappingStore, query: RollingAvailabilityMappingQuery): MutableList<RollingAvailabilityMapping> {
    return query.apply(store.list())
}
