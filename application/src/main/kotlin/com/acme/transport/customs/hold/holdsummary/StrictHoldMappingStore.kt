package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict hold mappings by code. */
interface StrictHoldMappingStore {
    fun find(code: String): StrictHoldMapping?
    fun save(item: StrictHoldMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictHoldMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictHoldMappingStore(
    var items: MutableMap<String, StrictHoldMapping>,
    var order: MutableList<String>,
) : StrictHoldMappingStore {
    override fun find(code: String): StrictHoldMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictHoldMapping) {
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

    override fun list(): MutableList<StrictHoldMapping> {
        val out = mutableListOf<StrictHoldMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictHoldMappingStore(): StrictHoldMappingStore {
    return MemoryStrictHoldMappingStore(mutableMapOf<String, StrictHoldMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictHoldMappingStore(
    var inner: StrictHoldMappingStore,
    var cache: TtlCache<StrictHoldMapping>,
    var hits: Int,
    var misses: Int,
) : StrictHoldMappingStore {
    override fun find(code: String): StrictHoldMapping? {
        val hit = (cache.get(code) as StrictHoldMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictHoldMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictHoldMapping> = inner.list()
}

fun newCachedStrictHoldMappingStore(inner: StrictHoldMappingStore, ttl: Int, clock: Clock): StrictHoldMappingStore {
    return CachedStrictHoldMappingStore(
        inner = inner,
        cache = newTtlCache<StrictHoldMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict hold mappings. Empty fields match all. */
data class StrictHoldMappingQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictHoldMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictHoldMapping>): MutableList<StrictHoldMapping> {
        val matched = filter(items) { candidate: StrictHoldMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictHoldMappings(store: StrictHoldMappingStore, query: StrictHoldMappingQuery): MutableList<StrictHoldMapping> {
    return query.apply(store.list())
}
