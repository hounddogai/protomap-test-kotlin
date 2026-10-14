package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling near miss mappings by code. */
interface RollingNearMissMappingStore {
    fun find(code: String): RollingNearMissMapping?
    fun save(item: RollingNearMissMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNearMissMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNearMissMappingStore(
    var items: MutableMap<String, RollingNearMissMapping>,
    var order: MutableList<String>,
) : RollingNearMissMappingStore {
    override fun find(code: String): RollingNearMissMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNearMissMapping) {
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

    override fun list(): MutableList<RollingNearMissMapping> {
        val out = mutableListOf<RollingNearMissMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNearMissMappingStore(): RollingNearMissMappingStore {
    return MemoryRollingNearMissMappingStore(
        mutableMapOf<String, RollingNearMissMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNearMissMappingStore(
    var inner: RollingNearMissMappingStore,
    var cache: TtlCache<RollingNearMissMapping>,
    var hits: Int,
    var misses: Int,
) : RollingNearMissMappingStore {
    override fun find(code: String): RollingNearMissMapping? {
        val hit = (cache.get(code) as RollingNearMissMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNearMissMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNearMissMapping> = inner.list()
}

fun newCachedRollingNearMissMappingStore(inner: RollingNearMissMappingStore, ttl: Int, clock: Clock): RollingNearMissMappingStore {
    return CachedRollingNearMissMappingStore(
        inner = inner,
        cache = newTtlCache<RollingNearMissMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling near miss mappings. Empty fields match all. */
data class RollingNearMissMappingQuery(
    var country: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNearMissMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNearMissMapping>): MutableList<RollingNearMissMapping> {
        val matched = filter(items) { candidate: RollingNearMissMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNearMissMappings(store: RollingNearMissMappingStore, query: RollingNearMissMappingQuery): MutableList<RollingNearMissMapping> {
    return query.apply(store.list())
}
