package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss adjustments by code. */
interface NearMissAdjustmentStore {
    fun find(code: String): NearMissAdjustment?
    fun save(item: NearMissAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissAdjustmentStore(
    var items: MutableMap<String, NearMissAdjustment>,
    var order: MutableList<String>,
) : NearMissAdjustmentStore {
    override fun find(code: String): NearMissAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissAdjustment) {
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

    override fun list(): MutableList<NearMissAdjustment> {
        val out = mutableListOf<NearMissAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissAdjustmentStore(): NearMissAdjustmentStore {
    return MemoryNearMissAdjustmentStore(mutableMapOf<String, NearMissAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissAdjustmentStore(
    var inner: NearMissAdjustmentStore,
    var cache: TtlCache<NearMissAdjustment>,
    var hits: Int,
    var misses: Int,
) : NearMissAdjustmentStore {
    override fun find(code: String): NearMissAdjustment? {
        val hit = (cache.get(code) as NearMissAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissAdjustment> = inner.list()
}

fun newCachedNearMissAdjustmentStore(inner: NearMissAdjustmentStore, ttl: Int, clock: Clock): NearMissAdjustmentStore {
    return CachedNearMissAdjustmentStore(
        inner = inner,
        cache = newTtlCache<NearMissAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss adjustments. Empty fields match all. */
data class NearMissAdjustmentQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissAdjustment>): MutableList<NearMissAdjustment> {
        val matched = filter(items) { candidate: NearMissAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissAdjustments(store: NearMissAdjustmentStore, query: NearMissAdjustmentQuery): MutableList<NearMissAdjustment> {
    return query.apply(store.list())
}
