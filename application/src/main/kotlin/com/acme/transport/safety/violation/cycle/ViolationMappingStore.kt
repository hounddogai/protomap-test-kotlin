package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation mappings by code. */
interface ViolationMappingStore {
    fun find(code: String): ViolationMapping?
    fun save(item: ViolationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationMappingStore(
    var items: MutableMap<String, ViolationMapping>,
    var order: MutableList<String>,
) : ViolationMappingStore {
    override fun find(code: String): ViolationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationMapping) {
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

    override fun list(): MutableList<ViolationMapping> {
        val out = mutableListOf<ViolationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationMappingStore(): ViolationMappingStore {
    return MemoryViolationMappingStore(mutableMapOf<String, ViolationMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationMappingStore(
    var inner: ViolationMappingStore,
    var cache: TtlCache<ViolationMapping>,
    var hits: Int,
    var misses: Int,
) : ViolationMappingStore {
    override fun find(code: String): ViolationMapping? {
        val hit = (cache.get(code) as ViolationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationMapping> = inner.list()
}

fun newCachedViolationMappingStore(inner: ViolationMappingStore, ttl: Int, clock: Clock): ViolationMappingStore {
    return CachedViolationMappingStore(
        inner = inner,
        cache = newTtlCache<ViolationMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation mappings. Empty fields match all. */
data class ViolationMappingQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ViolationMapping>): MutableList<ViolationMapping> {
        val matched = filter(items) { candidate: ViolationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationMappings(store: ViolationMappingStore, query: ViolationMappingQuery): MutableList<ViolationMapping> {
    return query.apply(store.list())
}
