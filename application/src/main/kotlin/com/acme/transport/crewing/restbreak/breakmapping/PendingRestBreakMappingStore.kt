package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending rest break mappings by code. */
interface PendingRestBreakMappingStore {
    fun find(code: String): PendingRestBreakMapping?
    fun save(item: PendingRestBreakMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRestBreakMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRestBreakMappingStore(
    var items: MutableMap<String, PendingRestBreakMapping>,
    var order: MutableList<String>,
) : PendingRestBreakMappingStore {
    override fun find(code: String): PendingRestBreakMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRestBreakMapping) {
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

    override fun list(): MutableList<PendingRestBreakMapping> {
        val out = mutableListOf<PendingRestBreakMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRestBreakMappingStore(): PendingRestBreakMappingStore {
    return MemoryPendingRestBreakMappingStore(
        mutableMapOf<String, PendingRestBreakMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRestBreakMappingStore(
    var inner: PendingRestBreakMappingStore,
    var cache: TtlCache<PendingRestBreakMapping>,
    var hits: Int,
    var misses: Int,
) : PendingRestBreakMappingStore {
    override fun find(code: String): PendingRestBreakMapping? {
        val hit = (cache.get(code) as PendingRestBreakMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRestBreakMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRestBreakMapping> = inner.list()
}

fun newCachedPendingRestBreakMappingStore(inner: PendingRestBreakMappingStore, ttl: Int, clock: Clock): PendingRestBreakMappingStore {
    return CachedPendingRestBreakMappingStore(
        inner = inner,
        cache = newTtlCache<PendingRestBreakMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending rest break mappings. Empty fields match all. */
data class PendingRestBreakMappingQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRestBreakMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRestBreakMapping>): MutableList<PendingRestBreakMapping> {
        val matched = filter(items) { candidate: PendingRestBreakMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRestBreakMappings(store: PendingRestBreakMappingStore, query: PendingRestBreakMappingQuery): MutableList<PendingRestBreakMapping> {
    return query.apply(store.list())
}
