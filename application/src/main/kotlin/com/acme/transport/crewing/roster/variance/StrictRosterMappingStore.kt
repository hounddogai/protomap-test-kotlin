package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster mappings by code. */
interface StrictRosterMappingStore {
    fun find(code: String): StrictRosterMapping?
    fun save(item: StrictRosterMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterMappingStore(
    var items: MutableMap<String, StrictRosterMapping>,
    var order: MutableList<String>,
) : StrictRosterMappingStore {
    override fun find(code: String): StrictRosterMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterMapping) {
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

    override fun list(): MutableList<StrictRosterMapping> {
        val out = mutableListOf<StrictRosterMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterMappingStore(): StrictRosterMappingStore {
    return MemoryStrictRosterMappingStore(mutableMapOf<String, StrictRosterMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterMappingStore(
    var inner: StrictRosterMappingStore,
    var cache: TtlCache<StrictRosterMapping>,
    var hits: Int,
    var misses: Int,
) : StrictRosterMappingStore {
    override fun find(code: String): StrictRosterMapping? {
        val hit = (cache.get(code) as StrictRosterMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterMapping> = inner.list()
}

fun newCachedStrictRosterMappingStore(inner: StrictRosterMappingStore, ttl: Int, clock: Clock): StrictRosterMappingStore {
    return CachedStrictRosterMappingStore(
        inner = inner,
        cache = newTtlCache<StrictRosterMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster mappings. Empty fields match all. */
data class StrictRosterMappingQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterMapping>): MutableList<StrictRosterMapping> {
        val matched = filter(items) { candidate: StrictRosterMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterMappings(store: StrictRosterMappingStore, query: StrictRosterMappingQuery): MutableList<StrictRosterMapping> {
    return query.apply(store.list())
}
