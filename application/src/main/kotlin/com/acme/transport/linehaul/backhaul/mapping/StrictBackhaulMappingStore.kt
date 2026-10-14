package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict backhaul mappings by code. */
interface StrictBackhaulMappingStore {
    fun find(code: String): StrictBackhaulMapping?
    fun save(item: StrictBackhaulMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBackhaulMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBackhaulMappingStore(
    var items: MutableMap<String, StrictBackhaulMapping>,
    var order: MutableList<String>,
) : StrictBackhaulMappingStore {
    override fun find(code: String): StrictBackhaulMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBackhaulMapping) {
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

    override fun list(): MutableList<StrictBackhaulMapping> {
        val out = mutableListOf<StrictBackhaulMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBackhaulMappingStore(): StrictBackhaulMappingStore {
    return MemoryStrictBackhaulMappingStore(
        mutableMapOf<String, StrictBackhaulMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBackhaulMappingStore(
    var inner: StrictBackhaulMappingStore,
    var cache: TtlCache<StrictBackhaulMapping>,
    var hits: Int,
    var misses: Int,
) : StrictBackhaulMappingStore {
    override fun find(code: String): StrictBackhaulMapping? {
        val hit = (cache.get(code) as StrictBackhaulMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBackhaulMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBackhaulMapping> = inner.list()
}

fun newCachedStrictBackhaulMappingStore(inner: StrictBackhaulMappingStore, ttl: Int, clock: Clock): StrictBackhaulMappingStore {
    return CachedStrictBackhaulMappingStore(
        inner = inner,
        cache = newTtlCache<StrictBackhaulMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict backhaul mappings. Empty fields match all. */
data class StrictBackhaulMappingQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBackhaulMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBackhaulMapping>): MutableList<StrictBackhaulMapping> {
        val matched = filter(items) { candidate: StrictBackhaulMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBackhaulMappings(store: StrictBackhaulMappingStore, query: StrictBackhaulMappingQuery): MutableList<StrictBackhaulMapping> {
    return query.apply(store.list())
}
