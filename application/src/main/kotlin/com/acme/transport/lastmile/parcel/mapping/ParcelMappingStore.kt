package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel mappings by code. */
interface ParcelMappingStore {
    fun find(code: String): ParcelMapping?
    fun save(item: ParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelMappingStore(
    var items: MutableMap<String, ParcelMapping>,
    var order: MutableList<String>,
) : ParcelMappingStore {
    override fun find(code: String): ParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelMapping) {
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

    override fun list(): MutableList<ParcelMapping> {
        val out = mutableListOf<ParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelMappingStore(): ParcelMappingStore {
    return MemoryParcelMappingStore(mutableMapOf<String, ParcelMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelMappingStore(
    var inner: ParcelMappingStore,
    var cache: TtlCache<ParcelMapping>,
    var hits: Int,
    var misses: Int,
) : ParcelMappingStore {
    override fun find(code: String): ParcelMapping? {
        val hit = (cache.get(code) as ParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelMapping> = inner.list()
}

fun newCachedParcelMappingStore(inner: ParcelMappingStore, ttl: Int, clock: Clock): ParcelMappingStore {
    return CachedParcelMappingStore(
        inner = inner,
        cache = newTtlCache<ParcelMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel mappings. Empty fields match all. */
data class ParcelMappingQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelMapping>): MutableList<ParcelMapping> {
        val matched = filter(items) { candidate: ParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelMappings(store: ParcelMappingStore, query: ParcelMappingQuery): MutableList<ParcelMapping> {
    return query.apply(store.list())
}
