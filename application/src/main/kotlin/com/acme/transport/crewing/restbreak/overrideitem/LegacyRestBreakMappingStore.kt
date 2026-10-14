package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rest break mappings by code. */
interface LegacyRestBreakMappingStore {
    fun find(code: String): LegacyRestBreakMapping?
    fun save(item: LegacyRestBreakMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRestBreakMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRestBreakMappingStore(
    var items: MutableMap<String, LegacyRestBreakMapping>,
    var order: MutableList<String>,
) : LegacyRestBreakMappingStore {
    override fun find(code: String): LegacyRestBreakMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRestBreakMapping) {
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

    override fun list(): MutableList<LegacyRestBreakMapping> {
        val out = mutableListOf<LegacyRestBreakMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRestBreakMappingStore(): LegacyRestBreakMappingStore {
    return MemoryLegacyRestBreakMappingStore(
        mutableMapOf<String, LegacyRestBreakMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRestBreakMappingStore(
    var inner: LegacyRestBreakMappingStore,
    var cache: TtlCache<LegacyRestBreakMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyRestBreakMappingStore {
    override fun find(code: String): LegacyRestBreakMapping? {
        val hit = (cache.get(code) as LegacyRestBreakMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRestBreakMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRestBreakMapping> = inner.list()
}

fun newCachedLegacyRestBreakMappingStore(inner: LegacyRestBreakMappingStore, ttl: Int, clock: Clock): LegacyRestBreakMappingStore {
    return CachedLegacyRestBreakMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyRestBreakMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rest break mappings. Empty fields match all. */
data class LegacyRestBreakMappingQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRestBreakMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRestBreakMapping>): MutableList<LegacyRestBreakMapping> {
        val matched = filter(items) { candidate: LegacyRestBreakMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRestBreakMappings(store: LegacyRestBreakMappingStore, query: LegacyRestBreakMappingQuery): MutableList<LegacyRestBreakMapping> {
    return query.apply(store.list())
}
