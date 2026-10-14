package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone bundles by code. */
interface ZoneBundleStore {
    fun find(code: String): ZoneBundle?
    fun save(item: ZoneBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneBundleStore(
    var items: MutableMap<String, ZoneBundle>,
    var order: MutableList<String>,
) : ZoneBundleStore {
    override fun find(code: String): ZoneBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneBundle) {
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

    override fun list(): MutableList<ZoneBundle> {
        val out = mutableListOf<ZoneBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneBundleStore(): ZoneBundleStore {
    return MemoryZoneBundleStore(mutableMapOf<String, ZoneBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneBundleStore(
    var inner: ZoneBundleStore,
    var cache: TtlCache<ZoneBundle>,
    var hits: Int,
    var misses: Int,
) : ZoneBundleStore {
    override fun find(code: String): ZoneBundle? {
        val hit = (cache.get(code) as ZoneBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneBundle> = inner.list()
}

fun newCachedZoneBundleStore(inner: ZoneBundleStore, ttl: Int, clock: Clock): ZoneBundleStore {
    return CachedZoneBundleStore(
        inner = inner,
        cache = newTtlCache<ZoneBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone bundles. Empty fields match all. */
data class ZoneBundleQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneBundle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneBundle>): MutableList<ZoneBundle> {
        val matched = filter(items) { candidate: ZoneBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneBundles(store: ZoneBundleStore, query: ZoneBundleQuery): MutableList<ZoneBundle> {
    return query.apply(store.list())
}
