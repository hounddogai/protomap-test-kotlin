package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock adjustments by code. */
interface CrossDockAdjustmentStore {
    fun find(code: String): CrossDockAdjustment?
    fun save(item: CrossDockAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockAdjustmentStore(
    var items: MutableMap<String, CrossDockAdjustment>,
    var order: MutableList<String>,
) : CrossDockAdjustmentStore {
    override fun find(code: String): CrossDockAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockAdjustment) {
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

    override fun list(): MutableList<CrossDockAdjustment> {
        val out = mutableListOf<CrossDockAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockAdjustmentStore(): CrossDockAdjustmentStore {
    return MemoryCrossDockAdjustmentStore(mutableMapOf<String, CrossDockAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockAdjustmentStore(
    var inner: CrossDockAdjustmentStore,
    var cache: TtlCache<CrossDockAdjustment>,
    var hits: Int,
    var misses: Int,
) : CrossDockAdjustmentStore {
    override fun find(code: String): CrossDockAdjustment? {
        val hit = (cache.get(code) as CrossDockAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockAdjustment> = inner.list()
}

fun newCachedCrossDockAdjustmentStore(inner: CrossDockAdjustmentStore, ttl: Int, clock: Clock): CrossDockAdjustmentStore {
    return CachedCrossDockAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CrossDockAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock adjustments. Empty fields match all. */
data class CrossDockAdjustmentQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockAdjustment>): MutableList<CrossDockAdjustment> {
        val matched = filter(items) { candidate: CrossDockAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockAdjustments(store: CrossDockAdjustmentStore, query: CrossDockAdjustmentQuery): MutableList<CrossDockAdjustment> {
    return query.apply(store.list())
}
