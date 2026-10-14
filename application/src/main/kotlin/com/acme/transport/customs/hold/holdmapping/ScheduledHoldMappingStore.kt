package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold mappings by code. */
interface ScheduledHoldMappingStore {
    fun find(code: String): ScheduledHoldMapping?
    fun save(item: ScheduledHoldMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldMappingStore(
    var items: MutableMap<String, ScheduledHoldMapping>,
    var order: MutableList<String>,
) : ScheduledHoldMappingStore {
    override fun find(code: String): ScheduledHoldMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldMapping) {
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

    override fun list(): MutableList<ScheduledHoldMapping> {
        val out = mutableListOf<ScheduledHoldMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldMappingStore(): ScheduledHoldMappingStore {
    return MemoryScheduledHoldMappingStore(
        mutableMapOf<String, ScheduledHoldMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldMappingStore(
    var inner: ScheduledHoldMappingStore,
    var cache: TtlCache<ScheduledHoldMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldMappingStore {
    override fun find(code: String): ScheduledHoldMapping? {
        val hit = (cache.get(code) as ScheduledHoldMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldMapping> = inner.list()
}

fun newCachedScheduledHoldMappingStore(inner: ScheduledHoldMappingStore, ttl: Int, clock: Clock): ScheduledHoldMappingStore {
    return CachedScheduledHoldMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold mappings. Empty fields match all. */
data class ScheduledHoldMappingQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldMapping>): MutableList<ScheduledHoldMapping> {
        val matched = filter(items) { candidate: ScheduledHoldMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldMappings(store: ScheduledHoldMappingStore, query: ScheduledHoldMappingQuery): MutableList<ScheduledHoldMapping> {
    return query.apply(store.list())
}
