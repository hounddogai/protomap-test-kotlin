package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback camera event adjustments by code. */
interface FallbackCameraEventAdjustmentStore {
    fun find(code: String): FallbackCameraEventAdjustment?
    fun save(item: FallbackCameraEventAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCameraEventAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCameraEventAdjustmentStore(
    var items: MutableMap<String, FallbackCameraEventAdjustment>,
    var order: MutableList<String>,
) : FallbackCameraEventAdjustmentStore {
    override fun find(code: String): FallbackCameraEventAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCameraEventAdjustment) {
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

    override fun list(): MutableList<FallbackCameraEventAdjustment> {
        val out = mutableListOf<FallbackCameraEventAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCameraEventAdjustmentStore(): FallbackCameraEventAdjustmentStore {
    return MemoryFallbackCameraEventAdjustmentStore(
        mutableMapOf<String, FallbackCameraEventAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCameraEventAdjustmentStore(
    var inner: FallbackCameraEventAdjustmentStore,
    var cache: TtlCache<FallbackCameraEventAdjustment>,
    var hits: Int,
    var misses: Int,
) : FallbackCameraEventAdjustmentStore {
    override fun find(code: String): FallbackCameraEventAdjustment? {
        val hit = (cache.get(code) as FallbackCameraEventAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCameraEventAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCameraEventAdjustment> = inner.list()
}

fun newCachedFallbackCameraEventAdjustmentStore(inner: FallbackCameraEventAdjustmentStore, ttl: Int, clock: Clock): FallbackCameraEventAdjustmentStore {
    return CachedFallbackCameraEventAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FallbackCameraEventAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback camera event adjustments. Empty fields match all. */
data class FallbackCameraEventAdjustmentQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCameraEventAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCameraEventAdjustment>): MutableList<FallbackCameraEventAdjustment> {
        val matched = filter(items) { candidate: FallbackCameraEventAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCameraEventAdjustments(store: FallbackCameraEventAdjustmentStore, query: FallbackCameraEventAdjustmentQuery): MutableList<FallbackCameraEventAdjustment> {
    return query.apply(store.list())
}
