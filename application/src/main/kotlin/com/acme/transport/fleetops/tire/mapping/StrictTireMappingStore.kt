package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict tire mappings by code. */
interface StrictTireMappingStore {
    fun find(code: String): StrictTireMapping?
    fun save(item: StrictTireMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTireMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTireMappingStore(
    var items: MutableMap<String, StrictTireMapping>,
    var order: MutableList<String>,
) : StrictTireMappingStore {
    override fun find(code: String): StrictTireMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTireMapping) {
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

    override fun list(): MutableList<StrictTireMapping> {
        val out = mutableListOf<StrictTireMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTireMappingStore(): StrictTireMappingStore {
    return MemoryStrictTireMappingStore(mutableMapOf<String, StrictTireMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTireMappingStore(
    var inner: StrictTireMappingStore,
    var cache: TtlCache<StrictTireMapping>,
    var hits: Int,
    var misses: Int,
) : StrictTireMappingStore {
    override fun find(code: String): StrictTireMapping? {
        val hit = (cache.get(code) as StrictTireMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTireMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTireMapping> = inner.list()
}

fun newCachedStrictTireMappingStore(inner: StrictTireMappingStore, ttl: Int, clock: Clock): StrictTireMappingStore {
    return CachedStrictTireMappingStore(
        inner = inner,
        cache = newTtlCache<StrictTireMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tire mappings. Empty fields match all. */
data class StrictTireMappingQuery(
    var depot: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTireMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTireMapping>): MutableList<StrictTireMapping> {
        val matched = filter(items) { candidate: StrictTireMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTireMappings(store: StrictTireMappingStore, query: StrictTireMappingQuery): MutableList<StrictTireMapping> {
    return query.apply(store.list())
}
