package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan mappings by code. */
interface LanePlanMappingStore {
    fun find(code: String): LanePlanMapping?
    fun save(item: LanePlanMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanMappingStore(
    var items: MutableMap<String, LanePlanMapping>,
    var order: MutableList<String>,
) : LanePlanMappingStore {
    override fun find(code: String): LanePlanMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanMapping) {
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

    override fun list(): MutableList<LanePlanMapping> {
        val out = mutableListOf<LanePlanMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanMappingStore(): LanePlanMappingStore {
    return MemoryLanePlanMappingStore(mutableMapOf<String, LanePlanMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanMappingStore(
    var inner: LanePlanMappingStore,
    var cache: TtlCache<LanePlanMapping>,
    var hits: Int,
    var misses: Int,
) : LanePlanMappingStore {
    override fun find(code: String): LanePlanMapping? {
        val hit = (cache.get(code) as LanePlanMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanMapping> = inner.list()
}

fun newCachedLanePlanMappingStore(inner: LanePlanMappingStore, ttl: Int, clock: Clock): LanePlanMappingStore {
    return CachedLanePlanMappingStore(
        inner = inner,
        cache = newTtlCache<LanePlanMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan mappings. Empty fields match all. */
data class LanePlanMappingQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanMapping>): MutableList<LanePlanMapping> {
        val matched = filter(items) { candidate: LanePlanMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanMappings(store: LanePlanMappingStore, query: LanePlanMappingQuery): MutableList<LanePlanMapping> {
    return query.apply(store.list())
}
