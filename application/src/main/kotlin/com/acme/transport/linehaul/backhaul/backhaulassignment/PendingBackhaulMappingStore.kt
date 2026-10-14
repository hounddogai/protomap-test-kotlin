package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul mappings by code. */
interface PendingBackhaulMappingStore {
    fun find(code: String): PendingBackhaulMapping?
    fun save(item: PendingBackhaulMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulMappingStore(
    var items: MutableMap<String, PendingBackhaulMapping>,
    var order: MutableList<String>,
) : PendingBackhaulMappingStore {
    override fun find(code: String): PendingBackhaulMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulMapping) {
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

    override fun list(): MutableList<PendingBackhaulMapping> {
        val out = mutableListOf<PendingBackhaulMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulMappingStore(): PendingBackhaulMappingStore {
    return MemoryPendingBackhaulMappingStore(
        mutableMapOf<String, PendingBackhaulMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulMappingStore(
    var inner: PendingBackhaulMappingStore,
    var cache: TtlCache<PendingBackhaulMapping>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulMappingStore {
    override fun find(code: String): PendingBackhaulMapping? {
        val hit = (cache.get(code) as PendingBackhaulMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulMapping> = inner.list()
}

fun newCachedPendingBackhaulMappingStore(inner: PendingBackhaulMappingStore, ttl: Int, clock: Clock): PendingBackhaulMappingStore {
    return CachedPendingBackhaulMappingStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul mappings. Empty fields match all. */
data class PendingBackhaulMappingQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PendingBackhaulMapping>): MutableList<PendingBackhaulMapping> {
        val matched = filter(items) { candidate: PendingBackhaulMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulMappings(store: PendingBackhaulMappingStore, query: PendingBackhaulMappingQuery): MutableList<PendingBackhaulMapping> {
    return query.apply(store.list())
}
