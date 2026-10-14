package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop mappings by code. */
interface DropMappingStore {
    fun find(code: String): DropMapping?
    fun save(item: DropMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropMappingStore(
    var items: MutableMap<String, DropMapping>,
    var order: MutableList<String>,
) : DropMappingStore {
    override fun find(code: String): DropMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropMapping) {
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

    override fun list(): MutableList<DropMapping> {
        val out = mutableListOf<DropMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropMappingStore(): DropMappingStore {
    return MemoryDropMappingStore(mutableMapOf<String, DropMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropMappingStore(
    var inner: DropMappingStore,
    var cache: TtlCache<DropMapping>,
    var hits: Int,
    var misses: Int,
) : DropMappingStore {
    override fun find(code: String): DropMapping? {
        val hit = (cache.get(code) as DropMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropMapping> = inner.list()
}

fun newCachedDropMappingStore(inner: DropMappingStore, ttl: Int, clock: Clock): DropMappingStore {
    return CachedDropMappingStore(
        inner = inner,
        cache = newTtlCache<DropMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop mappings. Empty fields match all. */
data class DropMappingQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropMapping>): MutableList<DropMapping> {
        val matched = filter(items) { candidate: DropMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropMappings(store: DropMappingStore, query: DropMappingQuery): MutableList<DropMapping> {
    return query.apply(store.list())
}
