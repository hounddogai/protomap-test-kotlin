package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted redelivery windows by code. */
interface WeightedRedeliveryWindowStore {
    fun find(code: String): WeightedRedeliveryWindow?
    fun save(item: WeightedRedeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRedeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRedeliveryWindowStore(
    var items: MutableMap<String, WeightedRedeliveryWindow>,
    var order: MutableList<String>,
) : WeightedRedeliveryWindowStore {
    override fun find(code: String): WeightedRedeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRedeliveryWindow) {
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

    override fun list(): MutableList<WeightedRedeliveryWindow> {
        val out = mutableListOf<WeightedRedeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRedeliveryWindowStore(): WeightedRedeliveryWindowStore {
    return MemoryWeightedRedeliveryWindowStore(
        mutableMapOf<String, WeightedRedeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRedeliveryWindowStore(
    var inner: WeightedRedeliveryWindowStore,
    var cache: TtlCache<WeightedRedeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : WeightedRedeliveryWindowStore {
    override fun find(code: String): WeightedRedeliveryWindow? {
        val hit = (cache.get(code) as WeightedRedeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRedeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRedeliveryWindow> = inner.list()
}

fun newCachedWeightedRedeliveryWindowStore(inner: WeightedRedeliveryWindowStore, ttl: Int, clock: Clock): WeightedRedeliveryWindowStore {
    return CachedWeightedRedeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<WeightedRedeliveryWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted redelivery windows. Empty fields match all. */
data class WeightedRedeliveryWindowQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRedeliveryWindow): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRedeliveryWindow>): MutableList<WeightedRedeliveryWindow> {
        val matched = filter(items) { candidate: WeightedRedeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRedeliveryWindows(store: WeightedRedeliveryWindowStore, query: WeightedRedeliveryWindowQuery): MutableList<WeightedRedeliveryWindow> {
    return query.apply(store.list())
}
