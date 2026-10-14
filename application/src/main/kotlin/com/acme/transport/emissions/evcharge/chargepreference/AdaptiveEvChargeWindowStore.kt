package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive ev charge windows by code. */
interface AdaptiveEvChargeWindowStore {
    fun find(code: String): AdaptiveEvChargeWindow?
    fun save(item: AdaptiveEvChargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEvChargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEvChargeWindowStore(
    var items: MutableMap<String, AdaptiveEvChargeWindow>,
    var order: MutableList<String>,
) : AdaptiveEvChargeWindowStore {
    override fun find(code: String): AdaptiveEvChargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEvChargeWindow) {
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

    override fun list(): MutableList<AdaptiveEvChargeWindow> {
        val out = mutableListOf<AdaptiveEvChargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEvChargeWindowStore(): AdaptiveEvChargeWindowStore {
    return MemoryAdaptiveEvChargeWindowStore(
        mutableMapOf<String, AdaptiveEvChargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEvChargeWindowStore(
    var inner: AdaptiveEvChargeWindowStore,
    var cache: TtlCache<AdaptiveEvChargeWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEvChargeWindowStore {
    override fun find(code: String): AdaptiveEvChargeWindow? {
        val hit = (cache.get(code) as AdaptiveEvChargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEvChargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEvChargeWindow> = inner.list()
}

fun newCachedAdaptiveEvChargeWindowStore(inner: AdaptiveEvChargeWindowStore, ttl: Int, clock: Clock): AdaptiveEvChargeWindowStore {
    return CachedAdaptiveEvChargeWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEvChargeWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive ev charge windows. Empty fields match all. */
data class AdaptiveEvChargeWindowQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEvChargeWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEvChargeWindow>): MutableList<AdaptiveEvChargeWindow> {
        val matched = filter(items) { candidate: AdaptiveEvChargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEvChargeWindows(store: AdaptiveEvChargeWindowStore, query: AdaptiveEvChargeWindowQuery): MutableList<AdaptiveEvChargeWindow> {
    return query.apply(store.list())
}
