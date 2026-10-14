package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped violation mappings by code. */
interface CappedViolationMappingStore {
    fun find(code: String): CappedViolationMapping?
    fun save(item: CappedViolationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedViolationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedViolationMappingStore(
    var items: MutableMap<String, CappedViolationMapping>,
    var order: MutableList<String>,
) : CappedViolationMappingStore {
    override fun find(code: String): CappedViolationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedViolationMapping) {
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

    override fun list(): MutableList<CappedViolationMapping> {
        val out = mutableListOf<CappedViolationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedViolationMappingStore(): CappedViolationMappingStore {
    return MemoryCappedViolationMappingStore(
        mutableMapOf<String, CappedViolationMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedViolationMappingStore(
    var inner: CappedViolationMappingStore,
    var cache: TtlCache<CappedViolationMapping>,
    var hits: Int,
    var misses: Int,
) : CappedViolationMappingStore {
    override fun find(code: String): CappedViolationMapping? {
        val hit = (cache.get(code) as CappedViolationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedViolationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedViolationMapping> = inner.list()
}

fun newCachedCappedViolationMappingStore(inner: CappedViolationMappingStore, ttl: Int, clock: Clock): CappedViolationMappingStore {
    return CachedCappedViolationMappingStore(
        inner = inner,
        cache = newTtlCache<CappedViolationMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped violation mappings. Empty fields match all. */
data class CappedViolationMappingQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedViolationMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedViolationMapping>): MutableList<CappedViolationMapping> {
        val matched = filter(items) { candidate: CappedViolationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedViolationMappings(store: CappedViolationMappingStore, query: CappedViolationMappingQuery): MutableList<CappedViolationMapping> {
    return query.apply(store.list())
}
