package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event estimates by code. */
interface CameraEventEstimateStore {
    fun find(code: String): CameraEventEstimate?
    fun save(item: CameraEventEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventEstimateStore(
    var items: MutableMap<String, CameraEventEstimate>,
    var order: MutableList<String>,
) : CameraEventEstimateStore {
    override fun find(code: String): CameraEventEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventEstimate) {
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

    override fun list(): MutableList<CameraEventEstimate> {
        val out = mutableListOf<CameraEventEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventEstimateStore(): CameraEventEstimateStore {
    return MemoryCameraEventEstimateStore(mutableMapOf<String, CameraEventEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventEstimateStore(
    var inner: CameraEventEstimateStore,
    var cache: TtlCache<CameraEventEstimate>,
    var hits: Int,
    var misses: Int,
) : CameraEventEstimateStore {
    override fun find(code: String): CameraEventEstimate? {
        val hit = (cache.get(code) as CameraEventEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventEstimate> = inner.list()
}

fun newCachedCameraEventEstimateStore(inner: CameraEventEstimateStore, ttl: Int, clock: Clock): CameraEventEstimateStore {
    return CachedCameraEventEstimateStore(
        inner = inner,
        cache = newTtlCache<CameraEventEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event estimates. Empty fields match all. */
data class CameraEventEstimateQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventEstimate): Boolean {
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

    fun apply(items: MutableList<CameraEventEstimate>): MutableList<CameraEventEstimate> {
        val matched = filter(items) { candidate: CameraEventEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventEstimates(store: CameraEventEstimateStore, query: CameraEventEstimateQuery): MutableList<CameraEventEstimate> {
    return query.apply(store.list())
}
