package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event requests by code. */
interface CameraEventRequestStore {
    fun find(code: String): CameraEventRequest?
    fun save(item: CameraEventRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventRequestStore(
    var items: MutableMap<String, CameraEventRequest>,
    var order: MutableList<String>,
) : CameraEventRequestStore {
    override fun find(code: String): CameraEventRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventRequest) {
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

    override fun list(): MutableList<CameraEventRequest> {
        val out = mutableListOf<CameraEventRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventRequestStore(): CameraEventRequestStore {
    return MemoryCameraEventRequestStore(mutableMapOf<String, CameraEventRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventRequestStore(
    var inner: CameraEventRequestStore,
    var cache: TtlCache<CameraEventRequest>,
    var hits: Int,
    var misses: Int,
) : CameraEventRequestStore {
    override fun find(code: String): CameraEventRequest? {
        val hit = (cache.get(code) as CameraEventRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventRequest> = inner.list()
}

fun newCachedCameraEventRequestStore(inner: CameraEventRequestStore, ttl: Int, clock: Clock): CameraEventRequestStore {
    return CachedCameraEventRequestStore(
        inner = inner,
        cache = newTtlCache<CameraEventRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event requests. Empty fields match all. */
data class CameraEventRequestQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventRequest>): MutableList<CameraEventRequest> {
        val matched = filter(items) { candidate: CameraEventRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventRequests(store: CameraEventRequestStore, query: CameraEventRequestQuery): MutableList<CameraEventRequest> {
    return query.apply(store.list())
}
