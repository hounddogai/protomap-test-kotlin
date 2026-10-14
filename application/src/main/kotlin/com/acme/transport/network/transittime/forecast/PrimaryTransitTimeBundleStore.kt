package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time bundles by code. */
interface PrimaryTransitTimeBundleStore {
    fun find(code: String): PrimaryTransitTimeBundle?
    fun save(item: PrimaryTransitTimeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeBundleStore(
    var items: MutableMap<String, PrimaryTransitTimeBundle>,
    var order: MutableList<String>,
) : PrimaryTransitTimeBundleStore {
    override fun find(code: String): PrimaryTransitTimeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeBundle) {
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

    override fun list(): MutableList<PrimaryTransitTimeBundle> {
        val out = mutableListOf<PrimaryTransitTimeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeBundleStore(): PrimaryTransitTimeBundleStore {
    return MemoryPrimaryTransitTimeBundleStore(
        mutableMapOf<String, PrimaryTransitTimeBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeBundleStore(
    var inner: PrimaryTransitTimeBundleStore,
    var cache: TtlCache<PrimaryTransitTimeBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeBundleStore {
    override fun find(code: String): PrimaryTransitTimeBundle? {
        val hit = (cache.get(code) as PrimaryTransitTimeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeBundle> = inner.list()
}

fun newCachedPrimaryTransitTimeBundleStore(inner: PrimaryTransitTimeBundleStore, ttl: Int, clock: Clock): PrimaryTransitTimeBundleStore {
    return CachedPrimaryTransitTimeBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time bundles. Empty fields match all. */
data class PrimaryTransitTimeBundleQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTransitTimeBundle>): MutableList<PrimaryTransitTimeBundle> {
        val matched = filter(items) { candidate: PrimaryTransitTimeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeBundles(store: PrimaryTransitTimeBundleStore, query: PrimaryTransitTimeBundleQuery): MutableList<PrimaryTransitTimeBundle> {
    return query.apply(store.list())
}
