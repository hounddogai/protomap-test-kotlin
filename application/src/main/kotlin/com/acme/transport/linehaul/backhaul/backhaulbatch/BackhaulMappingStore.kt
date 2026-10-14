package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul mappings by code. */
interface BackhaulMappingStore {
    fun find(code: String): BackhaulMapping?
    fun save(item: BackhaulMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulMappingStore(
    var items: MutableMap<String, BackhaulMapping>,
    var order: MutableList<String>,
) : BackhaulMappingStore {
    override fun find(code: String): BackhaulMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulMapping) {
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

    override fun list(): MutableList<BackhaulMapping> {
        val out = mutableListOf<BackhaulMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulMappingStore(): BackhaulMappingStore {
    return MemoryBackhaulMappingStore(mutableMapOf<String, BackhaulMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulMappingStore(
    var inner: BackhaulMappingStore,
    var cache: TtlCache<BackhaulMapping>,
    var hits: Int,
    var misses: Int,
) : BackhaulMappingStore {
    override fun find(code: String): BackhaulMapping? {
        val hit = (cache.get(code) as BackhaulMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulMapping> = inner.list()
}

fun newCachedBackhaulMappingStore(inner: BackhaulMappingStore, ttl: Int, clock: Clock): BackhaulMappingStore {
    return CachedBackhaulMappingStore(
        inner = inner,
        cache = newTtlCache<BackhaulMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul mappings. Empty fields match all. */
data class BackhaulMappingQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulMapping): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<BackhaulMapping>): MutableList<BackhaulMapping> {
        val matched = filter(items) { candidate: BackhaulMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulMappings(store: BackhaulMappingStore, query: BackhaulMappingQuery): MutableList<BackhaulMapping> {
    return query.apply(store.list())
}
