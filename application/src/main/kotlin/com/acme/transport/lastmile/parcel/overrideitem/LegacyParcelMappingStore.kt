package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy parcel mappings by code. */
interface LegacyParcelMappingStore {
    fun find(code: String): LegacyParcelMapping?
    fun save(item: LegacyParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyParcelMappingStore(
    var items: MutableMap<String, LegacyParcelMapping>,
    var order: MutableList<String>,
) : LegacyParcelMappingStore {
    override fun find(code: String): LegacyParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyParcelMapping) {
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

    override fun list(): MutableList<LegacyParcelMapping> {
        val out = mutableListOf<LegacyParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyParcelMappingStore(): LegacyParcelMappingStore {
    return MemoryLegacyParcelMappingStore(mutableMapOf<String, LegacyParcelMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyParcelMappingStore(
    var inner: LegacyParcelMappingStore,
    var cache: TtlCache<LegacyParcelMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyParcelMappingStore {
    override fun find(code: String): LegacyParcelMapping? {
        val hit = (cache.get(code) as LegacyParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyParcelMapping> = inner.list()
}

fun newCachedLegacyParcelMappingStore(inner: LegacyParcelMappingStore, ttl: Int, clock: Clock): LegacyParcelMappingStore {
    return CachedLegacyParcelMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyParcelMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy parcel mappings. Empty fields match all. */
data class LegacyParcelMappingQuery(
    var depot: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyParcelMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LegacyParcelMapping>): MutableList<LegacyParcelMapping> {
        val matched = filter(items) { candidate: LegacyParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyParcelMappings(store: LegacyParcelMappingStore, query: LegacyParcelMappingQuery): MutableList<LegacyParcelMapping> {
    return query.apply(store.list())
}
