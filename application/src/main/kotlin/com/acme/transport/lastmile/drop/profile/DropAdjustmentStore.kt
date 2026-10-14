package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop adjustments by code. */
interface DropAdjustmentStore {
    fun find(code: String): DropAdjustment?
    fun save(item: DropAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropAdjustmentStore(
    var items: MutableMap<String, DropAdjustment>,
    var order: MutableList<String>,
) : DropAdjustmentStore {
    override fun find(code: String): DropAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropAdjustment) {
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

    override fun list(): MutableList<DropAdjustment> {
        val out = mutableListOf<DropAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropAdjustmentStore(): DropAdjustmentStore {
    return MemoryDropAdjustmentStore(mutableMapOf<String, DropAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropAdjustmentStore(
    var inner: DropAdjustmentStore,
    var cache: TtlCache<DropAdjustment>,
    var hits: Int,
    var misses: Int,
) : DropAdjustmentStore {
    override fun find(code: String): DropAdjustment? {
        val hit = (cache.get(code) as DropAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropAdjustment> = inner.list()
}

fun newCachedDropAdjustmentStore(inner: DropAdjustmentStore, ttl: Int, clock: Clock): DropAdjustmentStore {
    return CachedDropAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DropAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop adjustments. Empty fields match all. */
data class DropAdjustmentQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropAdjustment>): MutableList<DropAdjustment> {
        val matched = filter(items) { candidate: DropAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropAdjustments(store: DropAdjustmentStore, query: DropAdjustmentQuery): MutableList<DropAdjustment> {
    return query.apply(store.list())
}
