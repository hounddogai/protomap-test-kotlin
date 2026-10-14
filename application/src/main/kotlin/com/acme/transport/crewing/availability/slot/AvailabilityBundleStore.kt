package com.acme.transport.crewing.availability.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability bundles by code. */
interface AvailabilityBundleStore {
    fun find(code: String): AvailabilityBundle?
    fun save(item: AvailabilityBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityBundleStore(
    var items: MutableMap<String, AvailabilityBundle>,
    var order: MutableList<String>,
) : AvailabilityBundleStore {
    override fun find(code: String): AvailabilityBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityBundle) {
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

    override fun list(): MutableList<AvailabilityBundle> {
        val out = mutableListOf<AvailabilityBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityBundleStore(): AvailabilityBundleStore {
    return MemoryAvailabilityBundleStore(mutableMapOf<String, AvailabilityBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityBundleStore(
    var inner: AvailabilityBundleStore,
    var cache: TtlCache<AvailabilityBundle>,
    var hits: Int,
    var misses: Int,
) : AvailabilityBundleStore {
    override fun find(code: String): AvailabilityBundle? {
        val hit = (cache.get(code) as AvailabilityBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityBundle> = inner.list()
}

fun newCachedAvailabilityBundleStore(inner: AvailabilityBundleStore, ttl: Int, clock: Clock): AvailabilityBundleStore {
    return CachedAvailabilityBundleStore(
        inner = inner,
        cache = newTtlCache<AvailabilityBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability bundles. Empty fields match all. */
data class AvailabilityBundleQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityBundle>): MutableList<AvailabilityBundle> {
        val matched = filter(items) { candidate: AvailabilityBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityBundles(store: AvailabilityBundleStore, query: AvailabilityBundleQuery): MutableList<AvailabilityBundle> {
    return query.apply(store.list())
}
