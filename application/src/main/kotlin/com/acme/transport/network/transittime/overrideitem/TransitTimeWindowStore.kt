package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time windows by code. */
interface TransitTimeWindowStore {
    fun find(code: String): TransitTimeWindow?
    fun save(item: TransitTimeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeWindowStore(
    var items: MutableMap<String, TransitTimeWindow>,
    var order: MutableList<String>,
) : TransitTimeWindowStore {
    override fun find(code: String): TransitTimeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeWindow) {
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

    override fun list(): MutableList<TransitTimeWindow> {
        val out = mutableListOf<TransitTimeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeWindowStore(): TransitTimeWindowStore {
    return MemoryTransitTimeWindowStore(mutableMapOf<String, TransitTimeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeWindowStore(
    var inner: TransitTimeWindowStore,
    var cache: TtlCache<TransitTimeWindow>,
    var hits: Int,
    var misses: Int,
) : TransitTimeWindowStore {
    override fun find(code: String): TransitTimeWindow? {
        val hit = (cache.get(code) as TransitTimeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeWindow> = inner.list()
}

fun newCachedTransitTimeWindowStore(inner: TransitTimeWindowStore, ttl: Int, clock: Clock): TransitTimeWindowStore {
    return CachedTransitTimeWindowStore(
        inner = inner,
        cache = newTtlCache<TransitTimeWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time windows. Empty fields match all. */
data class TransitTimeWindowQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeWindow>): MutableList<TransitTimeWindow> {
        val matched = filter(items) { candidate: TransitTimeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeWindows(store: TransitTimeWindowStore, query: TransitTimeWindowQuery): MutableList<TransitTimeWindow> {
    return query.apply(store.list())
}
