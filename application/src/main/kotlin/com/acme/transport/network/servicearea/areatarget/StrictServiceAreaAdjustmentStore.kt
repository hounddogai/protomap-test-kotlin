package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict service area adjustments by code. */
interface StrictServiceAreaAdjustmentStore {
    fun find(code: String): StrictServiceAreaAdjustment?
    fun save(item: StrictServiceAreaAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictServiceAreaAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictServiceAreaAdjustmentStore(
    var items: MutableMap<String, StrictServiceAreaAdjustment>,
    var order: MutableList<String>,
) : StrictServiceAreaAdjustmentStore {
    override fun find(code: String): StrictServiceAreaAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictServiceAreaAdjustment) {
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

    override fun list(): MutableList<StrictServiceAreaAdjustment> {
        val out = mutableListOf<StrictServiceAreaAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictServiceAreaAdjustmentStore(): StrictServiceAreaAdjustmentStore {
    return MemoryStrictServiceAreaAdjustmentStore(
        mutableMapOf<String, StrictServiceAreaAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictServiceAreaAdjustmentStore(
    var inner: StrictServiceAreaAdjustmentStore,
    var cache: TtlCache<StrictServiceAreaAdjustment>,
    var hits: Int,
    var misses: Int,
) : StrictServiceAreaAdjustmentStore {
    override fun find(code: String): StrictServiceAreaAdjustment? {
        val hit = (cache.get(code) as StrictServiceAreaAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictServiceAreaAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictServiceAreaAdjustment> = inner.list()
}

fun newCachedStrictServiceAreaAdjustmentStore(inner: StrictServiceAreaAdjustmentStore, ttl: Int, clock: Clock): StrictServiceAreaAdjustmentStore {
    return CachedStrictServiceAreaAdjustmentStore(
        inner = inner,
        cache = newTtlCache<StrictServiceAreaAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict service area adjustments. Empty fields match all. */
data class StrictServiceAreaAdjustmentQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictServiceAreaAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictServiceAreaAdjustment>): MutableList<StrictServiceAreaAdjustment> {
        val matched = filter(items) { candidate: StrictServiceAreaAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictServiceAreaAdjustments(store: StrictServiceAreaAdjustmentStore, query: StrictServiceAreaAdjustmentQuery): MutableList<StrictServiceAreaAdjustment> {
    return query.apply(store.list())
}
