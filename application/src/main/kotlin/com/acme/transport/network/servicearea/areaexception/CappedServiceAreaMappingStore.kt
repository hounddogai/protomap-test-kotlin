package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped service area mappings by code. */
interface CappedServiceAreaMappingStore {
    fun find(code: String): CappedServiceAreaMapping?
    fun save(item: CappedServiceAreaMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServiceAreaMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServiceAreaMappingStore(
    var items: MutableMap<String, CappedServiceAreaMapping>,
    var order: MutableList<String>,
) : CappedServiceAreaMappingStore {
    override fun find(code: String): CappedServiceAreaMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServiceAreaMapping) {
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

    override fun list(): MutableList<CappedServiceAreaMapping> {
        val out = mutableListOf<CappedServiceAreaMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServiceAreaMappingStore(): CappedServiceAreaMappingStore {
    return MemoryCappedServiceAreaMappingStore(
        mutableMapOf<String, CappedServiceAreaMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServiceAreaMappingStore(
    var inner: CappedServiceAreaMappingStore,
    var cache: TtlCache<CappedServiceAreaMapping>,
    var hits: Int,
    var misses: Int,
) : CappedServiceAreaMappingStore {
    override fun find(code: String): CappedServiceAreaMapping? {
        val hit = (cache.get(code) as CappedServiceAreaMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServiceAreaMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServiceAreaMapping> = inner.list()
}

fun newCachedCappedServiceAreaMappingStore(inner: CappedServiceAreaMappingStore, ttl: Int, clock: Clock): CappedServiceAreaMappingStore {
    return CachedCappedServiceAreaMappingStore(
        inner = inner,
        cache = newTtlCache<CappedServiceAreaMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service area mappings. Empty fields match all. */
data class CappedServiceAreaMappingQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServiceAreaMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CappedServiceAreaMapping>): MutableList<CappedServiceAreaMapping> {
        val matched = filter(items) { candidate: CappedServiceAreaMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServiceAreaMappings(store: CappedServiceAreaMappingStore, query: CappedServiceAreaMappingQuery): MutableList<CappedServiceAreaMapping> {
    return query.apply(store.list())
}
