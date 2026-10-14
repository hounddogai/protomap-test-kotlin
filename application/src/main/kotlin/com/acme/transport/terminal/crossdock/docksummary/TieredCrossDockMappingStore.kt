package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock mappings by code. */
interface TieredCrossDockMappingStore {
    fun find(code: String): TieredCrossDockMapping?
    fun save(item: TieredCrossDockMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockMappingStore(
    var items: MutableMap<String, TieredCrossDockMapping>,
    var order: MutableList<String>,
) : TieredCrossDockMappingStore {
    override fun find(code: String): TieredCrossDockMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockMapping) {
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

    override fun list(): MutableList<TieredCrossDockMapping> {
        val out = mutableListOf<TieredCrossDockMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockMappingStore(): TieredCrossDockMappingStore {
    return MemoryTieredCrossDockMappingStore(
        mutableMapOf<String, TieredCrossDockMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockMappingStore(
    var inner: TieredCrossDockMappingStore,
    var cache: TtlCache<TieredCrossDockMapping>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockMappingStore {
    override fun find(code: String): TieredCrossDockMapping? {
        val hit = (cache.get(code) as TieredCrossDockMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockMapping> = inner.list()
}

fun newCachedTieredCrossDockMappingStore(inner: TieredCrossDockMappingStore, ttl: Int, clock: Clock): TieredCrossDockMappingStore {
    return CachedTieredCrossDockMappingStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock mappings. Empty fields match all. */
data class TieredCrossDockMappingQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockMapping>): MutableList<TieredCrossDockMapping> {
        val matched = filter(items) { candidate: TieredCrossDockMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockMappings(store: TieredCrossDockMappingStore, query: TieredCrossDockMappingQuery): MutableList<TieredCrossDockMapping> {
    return query.apply(store.list())
}
