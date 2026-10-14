package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock mappings by code. */
interface CrossDockMappingStore {
    fun find(code: String): CrossDockMapping?
    fun save(item: CrossDockMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockMappingStore(
    var items: MutableMap<String, CrossDockMapping>,
    var order: MutableList<String>,
) : CrossDockMappingStore {
    override fun find(code: String): CrossDockMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockMapping) {
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

    override fun list(): MutableList<CrossDockMapping> {
        val out = mutableListOf<CrossDockMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockMappingStore(): CrossDockMappingStore {
    return MemoryCrossDockMappingStore(mutableMapOf<String, CrossDockMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockMappingStore(
    var inner: CrossDockMappingStore,
    var cache: TtlCache<CrossDockMapping>,
    var hits: Int,
    var misses: Int,
) : CrossDockMappingStore {
    override fun find(code: String): CrossDockMapping? {
        val hit = (cache.get(code) as CrossDockMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockMapping> = inner.list()
}

fun newCachedCrossDockMappingStore(inner: CrossDockMappingStore, ttl: Int, clock: Clock): CrossDockMappingStore {
    return CachedCrossDockMappingStore(
        inner = inner,
        cache = newTtlCache<CrossDockMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock mappings. Empty fields match all. */
data class CrossDockMappingQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockMapping): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CrossDockMapping>): MutableList<CrossDockMapping> {
        val matched = filter(items) { candidate: CrossDockMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockMappings(store: CrossDockMappingStore, query: CrossDockMappingQuery): MutableList<CrossDockMapping> {
    return query.apply(store.list())
}
