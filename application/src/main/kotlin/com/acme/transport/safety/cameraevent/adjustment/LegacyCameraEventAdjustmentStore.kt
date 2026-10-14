package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy camera event adjustments by code. */
interface LegacyCameraEventAdjustmentStore {
    fun find(code: String): LegacyCameraEventAdjustment?
    fun save(item: LegacyCameraEventAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCameraEventAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCameraEventAdjustmentStore(
    var items: MutableMap<String, LegacyCameraEventAdjustment>,
    var order: MutableList<String>,
) : LegacyCameraEventAdjustmentStore {
    override fun find(code: String): LegacyCameraEventAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCameraEventAdjustment) {
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

    override fun list(): MutableList<LegacyCameraEventAdjustment> {
        val out = mutableListOf<LegacyCameraEventAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCameraEventAdjustmentStore(): LegacyCameraEventAdjustmentStore {
    return MemoryLegacyCameraEventAdjustmentStore(
        mutableMapOf<String, LegacyCameraEventAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCameraEventAdjustmentStore(
    var inner: LegacyCameraEventAdjustmentStore,
    var cache: TtlCache<LegacyCameraEventAdjustment>,
    var hits: Int,
    var misses: Int,
) : LegacyCameraEventAdjustmentStore {
    override fun find(code: String): LegacyCameraEventAdjustment? {
        val hit = (cache.get(code) as LegacyCameraEventAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCameraEventAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCameraEventAdjustment> = inner.list()
}

fun newCachedLegacyCameraEventAdjustmentStore(inner: LegacyCameraEventAdjustmentStore, ttl: Int, clock: Clock): LegacyCameraEventAdjustmentStore {
    return CachedLegacyCameraEventAdjustmentStore(
        inner = inner,
        cache = newTtlCache<LegacyCameraEventAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy camera event adjustments. Empty fields match all. */
data class LegacyCameraEventAdjustmentQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCameraEventAdjustment): Boolean {
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

    fun apply(items: MutableList<LegacyCameraEventAdjustment>): MutableList<LegacyCameraEventAdjustment> {
        val matched = filter(items) { candidate: LegacyCameraEventAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCameraEventAdjustments(store: LegacyCameraEventAdjustmentStore, query: LegacyCameraEventAdjustmentQuery): MutableList<LegacyCameraEventAdjustment> {
    return query.apply(store.list())
}
