package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending zone mappings by code. */
interface PendingZoneMappingStore {
    fun find(code: String): PendingZoneMapping?
    fun save(item: PendingZoneMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingZoneMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingZoneMappingStore(
    var items: MutableMap<String, PendingZoneMapping>,
    var order: MutableList<String>,
) : PendingZoneMappingStore {
    override fun find(code: String): PendingZoneMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingZoneMapping) {
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

    override fun list(): MutableList<PendingZoneMapping> {
        val out = mutableListOf<PendingZoneMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingZoneMappingStore(): PendingZoneMappingStore {
    return MemoryPendingZoneMappingStore(mutableMapOf<String, PendingZoneMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingZoneMappingStore(
    var inner: PendingZoneMappingStore,
    var cache: TtlCache<PendingZoneMapping>,
    var hits: Int,
    var misses: Int,
) : PendingZoneMappingStore {
    override fun find(code: String): PendingZoneMapping? {
        val hit = (cache.get(code) as PendingZoneMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingZoneMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingZoneMapping> = inner.list()
}

fun newCachedPendingZoneMappingStore(inner: PendingZoneMappingStore, ttl: Int, clock: Clock): PendingZoneMappingStore {
    return CachedPendingZoneMappingStore(
        inner = inner,
        cache = newTtlCache<PendingZoneMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending zone mappings. Empty fields match all. */
data class PendingZoneMappingQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingZoneMapping): Boolean {
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

    fun apply(items: MutableList<PendingZoneMapping>): MutableList<PendingZoneMapping> {
        val matched = filter(items) { candidate: PendingZoneMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingZoneMappings(store: PendingZoneMappingStore, query: PendingZoneMappingQuery): MutableList<PendingZoneMapping> {
    return query.apply(store.list())
}
