package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient node mappings by code. */
interface LenientNodeMappingStore {
    fun find(code: String): LenientNodeMapping?
    fun save(item: LenientNodeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNodeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNodeMappingStore(
    var items: MutableMap<String, LenientNodeMapping>,
    var order: MutableList<String>,
) : LenientNodeMappingStore {
    override fun find(code: String): LenientNodeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNodeMapping) {
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

    override fun list(): MutableList<LenientNodeMapping> {
        val out = mutableListOf<LenientNodeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNodeMappingStore(): LenientNodeMappingStore {
    return MemoryLenientNodeMappingStore(mutableMapOf<String, LenientNodeMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNodeMappingStore(
    var inner: LenientNodeMappingStore,
    var cache: TtlCache<LenientNodeMapping>,
    var hits: Int,
    var misses: Int,
) : LenientNodeMappingStore {
    override fun find(code: String): LenientNodeMapping? {
        val hit = (cache.get(code) as LenientNodeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNodeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNodeMapping> = inner.list()
}

fun newCachedLenientNodeMappingStore(inner: LenientNodeMappingStore, ttl: Int, clock: Clock): LenientNodeMappingStore {
    return CachedLenientNodeMappingStore(
        inner = inner,
        cache = newTtlCache<LenientNodeMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient node mappings. Empty fields match all. */
data class LenientNodeMappingQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNodeMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientNodeMapping>): MutableList<LenientNodeMapping> {
        val matched = filter(items) { candidate: LenientNodeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNodeMappings(store: LenientNodeMappingStore, query: LenientNodeMappingQuery): MutableList<LenientNodeMapping> {
    return query.apply(store.list())
}
