package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling zone mappings by code. */
interface RollingZoneMappingStore {
    fun find(code: String): RollingZoneMapping?
    fun save(item: RollingZoneMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingZoneMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingZoneMappingStore(
    var items: MutableMap<String, RollingZoneMapping>,
    var order: MutableList<String>,
) : RollingZoneMappingStore {
    override fun find(code: String): RollingZoneMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingZoneMapping) {
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

    override fun list(): MutableList<RollingZoneMapping> {
        val out = mutableListOf<RollingZoneMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingZoneMappingStore(): RollingZoneMappingStore {
    return MemoryRollingZoneMappingStore(mutableMapOf<String, RollingZoneMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingZoneMappingStore(
    var inner: RollingZoneMappingStore,
    var cache: TtlCache<RollingZoneMapping>,
    var hits: Int,
    var misses: Int,
) : RollingZoneMappingStore {
    override fun find(code: String): RollingZoneMapping? {
        val hit = (cache.get(code) as RollingZoneMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingZoneMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingZoneMapping> = inner.list()
}

fun newCachedRollingZoneMappingStore(inner: RollingZoneMappingStore, ttl: Int, clock: Clock): RollingZoneMappingStore {
    return CachedRollingZoneMappingStore(
        inner = inner,
        cache = newTtlCache<RollingZoneMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling zone mappings. Empty fields match all. */
data class RollingZoneMappingQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingZoneMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingZoneMapping>): MutableList<RollingZoneMapping> {
        val matched = filter(items) { candidate: RollingZoneMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingZoneMappings(store: RollingZoneMappingStore, query: RollingZoneMappingQuery): MutableList<RollingZoneMapping> {
    return query.apply(store.list())
}
