package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient camera event metrics by code. */
interface LenientCameraEventMetricStore {
    fun find(code: String): LenientCameraEventMetric?
    fun save(item: LenientCameraEventMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCameraEventMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCameraEventMetricStore(
    var items: MutableMap<String, LenientCameraEventMetric>,
    var order: MutableList<String>,
) : LenientCameraEventMetricStore {
    override fun find(code: String): LenientCameraEventMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCameraEventMetric) {
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

    override fun list(): MutableList<LenientCameraEventMetric> {
        val out = mutableListOf<LenientCameraEventMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCameraEventMetricStore(): LenientCameraEventMetricStore {
    return MemoryLenientCameraEventMetricStore(
        mutableMapOf<String, LenientCameraEventMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCameraEventMetricStore(
    var inner: LenientCameraEventMetricStore,
    var cache: TtlCache<LenientCameraEventMetric>,
    var hits: Int,
    var misses: Int,
) : LenientCameraEventMetricStore {
    override fun find(code: String): LenientCameraEventMetric? {
        val hit = (cache.get(code) as LenientCameraEventMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCameraEventMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCameraEventMetric> = inner.list()
}

fun newCachedLenientCameraEventMetricStore(inner: LenientCameraEventMetricStore, ttl: Int, clock: Clock): LenientCameraEventMetricStore {
    return CachedLenientCameraEventMetricStore(
        inner = inner,
        cache = newTtlCache<LenientCameraEventMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient camera event metrics. Empty fields match all. */
data class LenientCameraEventMetricQuery(
    var country: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCameraEventMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCameraEventMetric>): MutableList<LenientCameraEventMetric> {
        val matched = filter(items) { candidate: LenientCameraEventMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCameraEventMetrics(store: LenientCameraEventMetricStore, query: LenientCameraEventMetricQuery): MutableList<LenientCameraEventMetric> {
    return query.apply(store.list())
}
