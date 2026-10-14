package com.acme.transport.crewing.roster.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default roster mappings by code. */
interface DefaultRosterMappingStore {
    fun find(code: String): DefaultRosterMapping?
    fun save(item: DefaultRosterMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRosterMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRosterMappingStore(
    var items: MutableMap<String, DefaultRosterMapping>,
    var order: MutableList<String>,
) : DefaultRosterMappingStore {
    override fun find(code: String): DefaultRosterMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRosterMapping) {
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

    override fun list(): MutableList<DefaultRosterMapping> {
        val out = mutableListOf<DefaultRosterMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRosterMappingStore(): DefaultRosterMappingStore {
    return MemoryDefaultRosterMappingStore(
        mutableMapOf<String, DefaultRosterMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRosterMappingStore(
    var inner: DefaultRosterMappingStore,
    var cache: TtlCache<DefaultRosterMapping>,
    var hits: Int,
    var misses: Int,
) : DefaultRosterMappingStore {
    override fun find(code: String): DefaultRosterMapping? {
        val hit = (cache.get(code) as DefaultRosterMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRosterMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRosterMapping> = inner.list()
}

fun newCachedDefaultRosterMappingStore(inner: DefaultRosterMappingStore, ttl: Int, clock: Clock): DefaultRosterMappingStore {
    return CachedDefaultRosterMappingStore(
        inner = inner,
        cache = newTtlCache<DefaultRosterMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default roster mappings. Empty fields match all. */
data class DefaultRosterMappingQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRosterMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRosterMapping>): MutableList<DefaultRosterMapping> {
        val matched = filter(items) { candidate: DefaultRosterMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRosterMappings(store: DefaultRosterMappingStore, query: DefaultRosterMappingQuery): MutableList<DefaultRosterMapping> {
    return query.apply(store.list())
}
