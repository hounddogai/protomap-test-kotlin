package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time mappings by code. */
interface TransitTimeMappingStore {
    fun find(code: String): TransitTimeMapping?
    fun save(item: TransitTimeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeMappingStore(
    var items: MutableMap<String, TransitTimeMapping>,
    var order: MutableList<String>,
) : TransitTimeMappingStore {
    override fun find(code: String): TransitTimeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeMapping) {
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

    override fun list(): MutableList<TransitTimeMapping> {
        val out = mutableListOf<TransitTimeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeMappingStore(): TransitTimeMappingStore {
    return MemoryTransitTimeMappingStore(mutableMapOf<String, TransitTimeMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeMappingStore(
    var inner: TransitTimeMappingStore,
    var cache: TtlCache<TransitTimeMapping>,
    var hits: Int,
    var misses: Int,
) : TransitTimeMappingStore {
    override fun find(code: String): TransitTimeMapping? {
        val hit = (cache.get(code) as TransitTimeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeMapping> = inner.list()
}

fun newCachedTransitTimeMappingStore(inner: TransitTimeMappingStore, ttl: Int, clock: Clock): TransitTimeMappingStore {
    return CachedTransitTimeMappingStore(
        inner = inner,
        cache = newTtlCache<TransitTimeMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time mappings. Empty fields match all. */
data class TransitTimeMappingQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeMapping>): MutableList<TransitTimeMapping> {
        val matched = filter(items) { candidate: TransitTimeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeMappings(store: TransitTimeMappingStore, query: TransitTimeMappingQuery): MutableList<TransitTimeMapping> {
    return query.apply(store.list())
}
