package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event segments by code. */
interface CameraEventSegmentStore {
    fun find(code: String): CameraEventSegment?
    fun save(item: CameraEventSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventSegmentStore(
    var items: MutableMap<String, CameraEventSegment>,
    var order: MutableList<String>,
) : CameraEventSegmentStore {
    override fun find(code: String): CameraEventSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventSegment) {
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

    override fun list(): MutableList<CameraEventSegment> {
        val out = mutableListOf<CameraEventSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventSegmentStore(): CameraEventSegmentStore {
    return MemoryCameraEventSegmentStore(mutableMapOf<String, CameraEventSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventSegmentStore(
    var inner: CameraEventSegmentStore,
    var cache: TtlCache<CameraEventSegment>,
    var hits: Int,
    var misses: Int,
) : CameraEventSegmentStore {
    override fun find(code: String): CameraEventSegment? {
        val hit = (cache.get(code) as CameraEventSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventSegment> = inner.list()
}

fun newCachedCameraEventSegmentStore(inner: CameraEventSegmentStore, ttl: Int, clock: Clock): CameraEventSegmentStore {
    return CachedCameraEventSegmentStore(
        inner = inner,
        cache = newTtlCache<CameraEventSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event segments. Empty fields match all. */
data class CameraEventSegmentQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventSegment>): MutableList<CameraEventSegment> {
        val matched = filter(items) { candidate: CameraEventSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventSegments(store: CameraEventSegmentStore, query: CameraEventSegmentQuery): MutableList<CameraEventSegment> {
    return query.apply(store.list())
}
