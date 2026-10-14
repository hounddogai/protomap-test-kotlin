package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door mappings by code. */
interface ScheduledDockDoorMappingStore {
    fun find(code: String): ScheduledDockDoorMapping?
    fun save(item: ScheduledDockDoorMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorMappingStore(
    var items: MutableMap<String, ScheduledDockDoorMapping>,
    var order: MutableList<String>,
) : ScheduledDockDoorMappingStore {
    override fun find(code: String): ScheduledDockDoorMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorMapping) {
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

    override fun list(): MutableList<ScheduledDockDoorMapping> {
        val out = mutableListOf<ScheduledDockDoorMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorMappingStore(): ScheduledDockDoorMappingStore {
    return MemoryScheduledDockDoorMappingStore(
        mutableMapOf<String, ScheduledDockDoorMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorMappingStore(
    var inner: ScheduledDockDoorMappingStore,
    var cache: TtlCache<ScheduledDockDoorMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorMappingStore {
    override fun find(code: String): ScheduledDockDoorMapping? {
        val hit = (cache.get(code) as ScheduledDockDoorMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorMapping> = inner.list()
}

fun newCachedScheduledDockDoorMappingStore(inner: ScheduledDockDoorMappingStore, ttl: Int, clock: Clock): ScheduledDockDoorMappingStore {
    return CachedScheduledDockDoorMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door mappings. Empty fields match all. */
data class ScheduledDockDoorMappingQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorMapping>): MutableList<ScheduledDockDoorMapping> {
        val matched = filter(items) { candidate: ScheduledDockDoorMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorMappings(store: ScheduledDockDoorMappingStore, query: ScheduledDockDoorMappingQuery): MutableList<ScheduledDockDoorMapping> {
    return query.apply(store.list())
}
