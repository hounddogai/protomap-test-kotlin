package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event adjustments by code. */
interface CameraEventAdjustmentStore {
    fun find(code: String): CameraEventAdjustment?
    fun save(item: CameraEventAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventAdjustmentStore(
    var items: MutableMap<String, CameraEventAdjustment>,
    var order: MutableList<String>,
) : CameraEventAdjustmentStore {
    override fun find(code: String): CameraEventAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventAdjustment) {
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

    override fun list(): MutableList<CameraEventAdjustment> {
        val out = mutableListOf<CameraEventAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventAdjustmentStore(): CameraEventAdjustmentStore {
    return MemoryCameraEventAdjustmentStore(
        mutableMapOf<String, CameraEventAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventAdjustmentStore(
    var inner: CameraEventAdjustmentStore,
    var cache: TtlCache<CameraEventAdjustment>,
    var hits: Int,
    var misses: Int,
) : CameraEventAdjustmentStore {
    override fun find(code: String): CameraEventAdjustment? {
        val hit = (cache.get(code) as CameraEventAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventAdjustment> = inner.list()
}

fun newCachedCameraEventAdjustmentStore(inner: CameraEventAdjustmentStore, ttl: Int, clock: Clock): CameraEventAdjustmentStore {
    return CachedCameraEventAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CameraEventAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event adjustments. Empty fields match all. */
data class CameraEventAdjustmentQuery(
    var unit: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventAdjustment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CameraEventAdjustment>): MutableList<CameraEventAdjustment> {
        val matched = filter(items) { candidate: CameraEventAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventAdjustments(store: CameraEventAdjustmentStore, query: CameraEventAdjustmentQuery): MutableList<CameraEventAdjustment> {
    return query.apply(store.list())
}
