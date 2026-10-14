package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel mappings by code. */
interface RegionalParcelMappingStore {
    fun find(code: String): RegionalParcelMapping?
    fun save(item: RegionalParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelMappingStore(
    var items: MutableMap<String, RegionalParcelMapping>,
    var order: MutableList<String>,
) : RegionalParcelMappingStore {
    override fun find(code: String): RegionalParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelMapping) {
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

    override fun list(): MutableList<RegionalParcelMapping> {
        val out = mutableListOf<RegionalParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelMappingStore(): RegionalParcelMappingStore {
    return MemoryRegionalParcelMappingStore(
        mutableMapOf<String, RegionalParcelMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelMappingStore(
    var inner: RegionalParcelMappingStore,
    var cache: TtlCache<RegionalParcelMapping>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelMappingStore {
    override fun find(code: String): RegionalParcelMapping? {
        val hit = (cache.get(code) as RegionalParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelMapping> = inner.list()
}

fun newCachedRegionalParcelMappingStore(inner: RegionalParcelMappingStore, ttl: Int, clock: Clock): RegionalParcelMappingStore {
    return CachedRegionalParcelMappingStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel mappings. Empty fields match all. */
data class RegionalParcelMappingQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelMapping): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<RegionalParcelMapping>): MutableList<RegionalParcelMapping> {
        val matched = filter(items) { candidate: RegionalParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelMappings(store: RegionalParcelMappingStore, query: RegionalParcelMappingQuery): MutableList<RegionalParcelMapping> {
    return query.apply(store.list())
}
