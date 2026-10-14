package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary hold mappings by code. */
interface PrimaryHoldMappingStore {
    fun find(code: String): PrimaryHoldMapping?
    fun save(item: PrimaryHoldMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHoldMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHoldMappingStore(
    var items: MutableMap<String, PrimaryHoldMapping>,
    var order: MutableList<String>,
) : PrimaryHoldMappingStore {
    override fun find(code: String): PrimaryHoldMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHoldMapping) {
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

    override fun list(): MutableList<PrimaryHoldMapping> {
        val out = mutableListOf<PrimaryHoldMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHoldMappingStore(): PrimaryHoldMappingStore {
    return MemoryPrimaryHoldMappingStore(mutableMapOf<String, PrimaryHoldMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHoldMappingStore(
    var inner: PrimaryHoldMappingStore,
    var cache: TtlCache<PrimaryHoldMapping>,
    var hits: Int,
    var misses: Int,
) : PrimaryHoldMappingStore {
    override fun find(code: String): PrimaryHoldMapping? {
        val hit = (cache.get(code) as PrimaryHoldMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHoldMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHoldMapping> = inner.list()
}

fun newCachedPrimaryHoldMappingStore(inner: PrimaryHoldMappingStore, ttl: Int, clock: Clock): PrimaryHoldMappingStore {
    return CachedPrimaryHoldMappingStore(
        inner = inner,
        cache = newTtlCache<PrimaryHoldMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hold mappings. Empty fields match all. */
data class PrimaryHoldMappingQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHoldMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHoldMapping>): MutableList<PrimaryHoldMapping> {
        val matched = filter(items) { candidate: PrimaryHoldMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHoldMappings(store: PrimaryHoldMappingStore, query: PrimaryHoldMappingQuery): MutableList<PrimaryHoldMapping> {
    return query.apply(store.list())
}
