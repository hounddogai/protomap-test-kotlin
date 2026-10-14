package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time bundles by code. */
interface TransitTimeBundleStore {
    fun find(code: String): TransitTimeBundle?
    fun save(item: TransitTimeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeBundleStore(
    var items: MutableMap<String, TransitTimeBundle>,
    var order: MutableList<String>,
) : TransitTimeBundleStore {
    override fun find(code: String): TransitTimeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeBundle) {
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

    override fun list(): MutableList<TransitTimeBundle> {
        val out = mutableListOf<TransitTimeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeBundleStore(): TransitTimeBundleStore {
    return MemoryTransitTimeBundleStore(mutableMapOf<String, TransitTimeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeBundleStore(
    var inner: TransitTimeBundleStore,
    var cache: TtlCache<TransitTimeBundle>,
    var hits: Int,
    var misses: Int,
) : TransitTimeBundleStore {
    override fun find(code: String): TransitTimeBundle? {
        val hit = (cache.get(code) as TransitTimeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeBundle> = inner.list()
}

fun newCachedTransitTimeBundleStore(inner: TransitTimeBundleStore, ttl: Int, clock: Clock): TransitTimeBundleStore {
    return CachedTransitTimeBundleStore(
        inner = inner,
        cache = newTtlCache<TransitTimeBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time bundles. Empty fields match all. */
data class TransitTimeBundleQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TransitTimeBundle>): MutableList<TransitTimeBundle> {
        val matched = filter(items) { candidate: TransitTimeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeBundles(store: TransitTimeBundleStore, query: TransitTimeBundleQuery): MutableList<TransitTimeBundle> {
    return query.apply(store.list())
}
