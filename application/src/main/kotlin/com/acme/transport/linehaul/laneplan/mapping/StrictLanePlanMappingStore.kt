package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict lane plan mappings by code. */
interface StrictLanePlanMappingStore {
    fun find(code: String): StrictLanePlanMapping?
    fun save(item: StrictLanePlanMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLanePlanMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLanePlanMappingStore(
    var items: MutableMap<String, StrictLanePlanMapping>,
    var order: MutableList<String>,
) : StrictLanePlanMappingStore {
    override fun find(code: String): StrictLanePlanMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLanePlanMapping) {
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

    override fun list(): MutableList<StrictLanePlanMapping> {
        val out = mutableListOf<StrictLanePlanMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLanePlanMappingStore(): StrictLanePlanMappingStore {
    return MemoryStrictLanePlanMappingStore(
        mutableMapOf<String, StrictLanePlanMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLanePlanMappingStore(
    var inner: StrictLanePlanMappingStore,
    var cache: TtlCache<StrictLanePlanMapping>,
    var hits: Int,
    var misses: Int,
) : StrictLanePlanMappingStore {
    override fun find(code: String): StrictLanePlanMapping? {
        val hit = (cache.get(code) as StrictLanePlanMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLanePlanMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLanePlanMapping> = inner.list()
}

fun newCachedStrictLanePlanMappingStore(inner: StrictLanePlanMappingStore, ttl: Int, clock: Clock): StrictLanePlanMappingStore {
    return CachedStrictLanePlanMappingStore(
        inner = inner,
        cache = newTtlCache<StrictLanePlanMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict lane plan mappings. Empty fields match all. */
data class StrictLanePlanMappingQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLanePlanMapping): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLanePlanMapping>): MutableList<StrictLanePlanMapping> {
        val matched = filter(items) { candidate: StrictLanePlanMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLanePlanMappings(store: StrictLanePlanMappingStore, query: StrictLanePlanMappingQuery): MutableList<StrictLanePlanMapping> {
    return query.apply(store.list())
}
