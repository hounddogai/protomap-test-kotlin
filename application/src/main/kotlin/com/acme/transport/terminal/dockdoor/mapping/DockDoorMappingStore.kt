package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door mappings by code. */
interface DockDoorMappingStore {
    fun find(code: String): DockDoorMapping?
    fun save(item: DockDoorMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorMappingStore(
    var items: MutableMap<String, DockDoorMapping>,
    var order: MutableList<String>,
) : DockDoorMappingStore {
    override fun find(code: String): DockDoorMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorMapping) {
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

    override fun list(): MutableList<DockDoorMapping> {
        val out = mutableListOf<DockDoorMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorMappingStore(): DockDoorMappingStore {
    return MemoryDockDoorMappingStore(mutableMapOf<String, DockDoorMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorMappingStore(
    var inner: DockDoorMappingStore,
    var cache: TtlCache<DockDoorMapping>,
    var hits: Int,
    var misses: Int,
) : DockDoorMappingStore {
    override fun find(code: String): DockDoorMapping? {
        val hit = (cache.get(code) as DockDoorMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorMapping> = inner.list()
}

fun newCachedDockDoorMappingStore(inner: DockDoorMappingStore, ttl: Int, clock: Clock): DockDoorMappingStore {
    return CachedDockDoorMappingStore(
        inner = inner,
        cache = newTtlCache<DockDoorMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door mappings. Empty fields match all. */
data class DockDoorMappingQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorMapping>): MutableList<DockDoorMapping> {
        val matched = filter(items) { candidate: DockDoorMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorMappings(store: DockDoorMappingStore, query: DockDoorMappingQuery): MutableList<DockDoorMapping> {
    return query.apply(store.list())
}
