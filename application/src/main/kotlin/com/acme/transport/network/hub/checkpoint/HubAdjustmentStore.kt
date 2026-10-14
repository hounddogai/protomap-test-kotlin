package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub adjustments by code. */
interface HubAdjustmentStore {
    fun find(code: String): HubAdjustment?
    fun save(item: HubAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubAdjustmentStore(
    var items: MutableMap<String, HubAdjustment>,
    var order: MutableList<String>,
) : HubAdjustmentStore {
    override fun find(code: String): HubAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubAdjustment) {
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

    override fun list(): MutableList<HubAdjustment> {
        val out = mutableListOf<HubAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubAdjustmentStore(): HubAdjustmentStore {
    return MemoryHubAdjustmentStore(mutableMapOf<String, HubAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubAdjustmentStore(
    var inner: HubAdjustmentStore,
    var cache: TtlCache<HubAdjustment>,
    var hits: Int,
    var misses: Int,
) : HubAdjustmentStore {
    override fun find(code: String): HubAdjustment? {
        val hit = (cache.get(code) as HubAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubAdjustment> = inner.list()
}

fun newCachedHubAdjustmentStore(inner: HubAdjustmentStore, ttl: Int, clock: Clock): HubAdjustmentStore {
    return CachedHubAdjustmentStore(
        inner = inner,
        cache = newTtlCache<HubAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub adjustments. Empty fields match all. */
data class HubAdjustmentQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubAdjustment>): MutableList<HubAdjustment> {
        val matched = filter(items) { candidate: HubAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubAdjustments(store: HubAdjustmentStore, query: HubAdjustmentQuery): MutableList<HubAdjustment> {
    return query.apply(store.list())
}
