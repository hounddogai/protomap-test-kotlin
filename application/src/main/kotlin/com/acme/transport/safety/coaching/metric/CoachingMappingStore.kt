package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching mappings by code. */
interface CoachingMappingStore {
    fun find(code: String): CoachingMapping?
    fun save(item: CoachingMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingMappingStore(
    var items: MutableMap<String, CoachingMapping>,
    var order: MutableList<String>,
) : CoachingMappingStore {
    override fun find(code: String): CoachingMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingMapping) {
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

    override fun list(): MutableList<CoachingMapping> {
        val out = mutableListOf<CoachingMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingMappingStore(): CoachingMappingStore {
    return MemoryCoachingMappingStore(mutableMapOf<String, CoachingMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingMappingStore(
    var inner: CoachingMappingStore,
    var cache: TtlCache<CoachingMapping>,
    var hits: Int,
    var misses: Int,
) : CoachingMappingStore {
    override fun find(code: String): CoachingMapping? {
        val hit = (cache.get(code) as CoachingMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingMapping> = inner.list()
}

fun newCachedCoachingMappingStore(inner: CoachingMappingStore, ttl: Int, clock: Clock): CoachingMappingStore {
    return CachedCoachingMappingStore(
        inner = inner,
        cache = newTtlCache<CoachingMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching mappings. Empty fields match all. */
data class CoachingMappingQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingMapping>): MutableList<CoachingMapping> {
        val matched = filter(items) { candidate: CoachingMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingMappings(store: CoachingMappingStore, query: CoachingMappingQuery): MutableList<CoachingMapping> {
    return query.apply(store.list())
}
