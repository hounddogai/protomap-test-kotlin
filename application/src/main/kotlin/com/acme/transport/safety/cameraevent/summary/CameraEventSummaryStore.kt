package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event summaries by code. */
interface CameraEventSummaryStore {
    fun find(code: String): CameraEventSummary?
    fun save(item: CameraEventSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventSummaryStore(
    var items: MutableMap<String, CameraEventSummary>,
    var order: MutableList<String>,
) : CameraEventSummaryStore {
    override fun find(code: String): CameraEventSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventSummary) {
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

    override fun list(): MutableList<CameraEventSummary> {
        val out = mutableListOf<CameraEventSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventSummaryStore(): CameraEventSummaryStore {
    return MemoryCameraEventSummaryStore(mutableMapOf<String, CameraEventSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventSummaryStore(
    var inner: CameraEventSummaryStore,
    var cache: TtlCache<CameraEventSummary>,
    var hits: Int,
    var misses: Int,
) : CameraEventSummaryStore {
    override fun find(code: String): CameraEventSummary? {
        val hit = (cache.get(code) as CameraEventSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventSummary> = inner.list()
}

fun newCachedCameraEventSummaryStore(inner: CameraEventSummaryStore, ttl: Int, clock: Clock): CameraEventSummaryStore {
    return CachedCameraEventSummaryStore(
        inner = inner,
        cache = newTtlCache<CameraEventSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event summaries. Empty fields match all. */
data class CameraEventSummaryQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<CameraEventSummary>): MutableList<CameraEventSummary> {
        val matched = filter(items) { candidate: CameraEventSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventSummaries(store: CameraEventSummaryStore, query: CameraEventSummaryQuery): MutableList<CameraEventSummary> {
    return query.apply(store.list())
}
