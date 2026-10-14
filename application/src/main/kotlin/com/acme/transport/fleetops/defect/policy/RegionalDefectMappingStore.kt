package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect mappings by code. */
interface RegionalDefectMappingStore {
    fun find(code: String): RegionalDefectMapping?
    fun save(item: RegionalDefectMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectMappingStore(
    var items: MutableMap<String, RegionalDefectMapping>,
    var order: MutableList<String>,
) : RegionalDefectMappingStore {
    override fun find(code: String): RegionalDefectMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectMapping) {
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

    override fun list(): MutableList<RegionalDefectMapping> {
        val out = mutableListOf<RegionalDefectMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectMappingStore(): RegionalDefectMappingStore {
    return MemoryRegionalDefectMappingStore(
        mutableMapOf<String, RegionalDefectMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectMappingStore(
    var inner: RegionalDefectMappingStore,
    var cache: TtlCache<RegionalDefectMapping>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectMappingStore {
    override fun find(code: String): RegionalDefectMapping? {
        val hit = (cache.get(code) as RegionalDefectMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectMapping> = inner.list()
}

fun newCachedRegionalDefectMappingStore(inner: RegionalDefectMappingStore, ttl: Int, clock: Clock): RegionalDefectMappingStore {
    return CachedRegionalDefectMappingStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect mappings. Empty fields match all. */
data class RegionalDefectMappingQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDefectMapping>): MutableList<RegionalDefectMapping> {
        val matched = filter(items) { candidate: RegionalDefectMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectMappings(store: RegionalDefectMappingStore, query: RegionalDefectMappingQuery): MutableList<RegionalDefectMapping> {
    return query.apply(store.list())
}
