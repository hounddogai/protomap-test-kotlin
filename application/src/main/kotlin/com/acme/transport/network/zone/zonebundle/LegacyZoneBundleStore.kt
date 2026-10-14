package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy zone bundles by code. */
interface LegacyZoneBundleStore {
    fun find(code: String): LegacyZoneBundle?
    fun save(item: LegacyZoneBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyZoneBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyZoneBundleStore(
    var items: MutableMap<String, LegacyZoneBundle>,
    var order: MutableList<String>,
) : LegacyZoneBundleStore {
    override fun find(code: String): LegacyZoneBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyZoneBundle) {
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

    override fun list(): MutableList<LegacyZoneBundle> {
        val out = mutableListOf<LegacyZoneBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyZoneBundleStore(): LegacyZoneBundleStore {
    return MemoryLegacyZoneBundleStore(mutableMapOf<String, LegacyZoneBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyZoneBundleStore(
    var inner: LegacyZoneBundleStore,
    var cache: TtlCache<LegacyZoneBundle>,
    var hits: Int,
    var misses: Int,
) : LegacyZoneBundleStore {
    override fun find(code: String): LegacyZoneBundle? {
        val hit = (cache.get(code) as LegacyZoneBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyZoneBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyZoneBundle> = inner.list()
}

fun newCachedLegacyZoneBundleStore(inner: LegacyZoneBundleStore, ttl: Int, clock: Clock): LegacyZoneBundleStore {
    return CachedLegacyZoneBundleStore(
        inner = inner,
        cache = newTtlCache<LegacyZoneBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy zone bundles. Empty fields match all. */
data class LegacyZoneBundleQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyZoneBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<LegacyZoneBundle>): MutableList<LegacyZoneBundle> {
        val matched = filter(items) { candidate: LegacyZoneBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyZoneBundles(store: LegacyZoneBundleStore, query: LegacyZoneBundleQuery): MutableList<LegacyZoneBundle> {
    return query.apply(store.list())
}
