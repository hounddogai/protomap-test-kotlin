package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered parcel mappings by code. */
interface TieredParcelMappingStore {
    fun find(code: String): TieredParcelMapping?
    fun save(item: TieredParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredParcelMappingStore(
    var items: MutableMap<String, TieredParcelMapping>,
    var order: MutableList<String>,
) : TieredParcelMappingStore {
    override fun find(code: String): TieredParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredParcelMapping) {
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

    override fun list(): MutableList<TieredParcelMapping> {
        val out = mutableListOf<TieredParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredParcelMappingStore(): TieredParcelMappingStore {
    return MemoryTieredParcelMappingStore(mutableMapOf<String, TieredParcelMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredParcelMappingStore(
    var inner: TieredParcelMappingStore,
    var cache: TtlCache<TieredParcelMapping>,
    var hits: Int,
    var misses: Int,
) : TieredParcelMappingStore {
    override fun find(code: String): TieredParcelMapping? {
        val hit = (cache.get(code) as TieredParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredParcelMapping> = inner.list()
}

fun newCachedTieredParcelMappingStore(inner: TieredParcelMappingStore, ttl: Int, clock: Clock): TieredParcelMappingStore {
    return CachedTieredParcelMappingStore(
        inner = inner,
        cache = newTtlCache<TieredParcelMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered parcel mappings. Empty fields match all. */
data class TieredParcelMappingQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredParcelMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredParcelMapping>): MutableList<TieredParcelMapping> {
        val matched = filter(items) { candidate: TieredParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredParcelMappings(store: TieredParcelMappingStore, query: TieredParcelMappingQuery): MutableList<TieredParcelMapping> {
    return query.apply(store.list())
}
