package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time mappings by code. */
interface PrimaryTransitTimeMappingStore {
    fun find(code: String): PrimaryTransitTimeMapping?
    fun save(item: PrimaryTransitTimeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeMappingStore(
    var items: MutableMap<String, PrimaryTransitTimeMapping>,
    var order: MutableList<String>,
) : PrimaryTransitTimeMappingStore {
    override fun find(code: String): PrimaryTransitTimeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeMapping) {
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

    override fun list(): MutableList<PrimaryTransitTimeMapping> {
        val out = mutableListOf<PrimaryTransitTimeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeMappingStore(): PrimaryTransitTimeMappingStore {
    return MemoryPrimaryTransitTimeMappingStore(
        mutableMapOf<String, PrimaryTransitTimeMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeMappingStore(
    var inner: PrimaryTransitTimeMappingStore,
    var cache: TtlCache<PrimaryTransitTimeMapping>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeMappingStore {
    override fun find(code: String): PrimaryTransitTimeMapping? {
        val hit = (cache.get(code) as PrimaryTransitTimeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeMapping> = inner.list()
}

fun newCachedPrimaryTransitTimeMappingStore(inner: PrimaryTransitTimeMappingStore, ttl: Int, clock: Clock): PrimaryTransitTimeMappingStore {
    return CachedPrimaryTransitTimeMappingStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time mappings. Empty fields match all. */
data class PrimaryTransitTimeMappingQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTransitTimeMapping>): MutableList<PrimaryTransitTimeMapping> {
        val matched = filter(items) { candidate: PrimaryTransitTimeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeMappings(store: PrimaryTransitTimeMappingStore, query: PrimaryTransitTimeMappingQuery): MutableList<PrimaryTransitTimeMapping> {
    return query.apply(store.list())
}
