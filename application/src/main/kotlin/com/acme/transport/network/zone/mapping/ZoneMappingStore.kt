package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone mappings by code. */
interface ZoneMappingStore {
    fun find(code: String): ZoneMapping?
    fun save(item: ZoneMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneMappingStore(
    var items: MutableMap<String, ZoneMapping>,
    var order: MutableList<String>,
) : ZoneMappingStore {
    override fun find(code: String): ZoneMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneMapping) {
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

    override fun list(): MutableList<ZoneMapping> {
        val out = mutableListOf<ZoneMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneMappingStore(): ZoneMappingStore {
    return MemoryZoneMappingStore(mutableMapOf<String, ZoneMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneMappingStore(
    var inner: ZoneMappingStore,
    var cache: TtlCache<ZoneMapping>,
    var hits: Int,
    var misses: Int,
) : ZoneMappingStore {
    override fun find(code: String): ZoneMapping? {
        val hit = (cache.get(code) as ZoneMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneMapping> = inner.list()
}

fun newCachedZoneMappingStore(inner: ZoneMappingStore, ttl: Int, clock: Clock): ZoneMappingStore {
    return CachedZoneMappingStore(
        inner = inner,
        cache = newTtlCache<ZoneMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone mappings. Empty fields match all. */
data class ZoneMappingQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneMapping>): MutableList<ZoneMapping> {
        val matched = filter(items) { candidate: ZoneMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneMappings(store: ZoneMappingStore, query: ZoneMappingQuery): MutableList<ZoneMapping> {
    return query.apply(store.list())
}
