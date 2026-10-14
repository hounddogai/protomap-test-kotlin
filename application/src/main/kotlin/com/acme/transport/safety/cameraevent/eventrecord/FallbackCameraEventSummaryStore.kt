package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback camera event summaries by code. */
interface FallbackCameraEventSummaryStore {
    fun find(code: String): FallbackCameraEventSummary?
    fun save(item: FallbackCameraEventSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCameraEventSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCameraEventSummaryStore(
    var items: MutableMap<String, FallbackCameraEventSummary>,
    var order: MutableList<String>,
) : FallbackCameraEventSummaryStore {
    override fun find(code: String): FallbackCameraEventSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCameraEventSummary) {
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

    override fun list(): MutableList<FallbackCameraEventSummary> {
        val out = mutableListOf<FallbackCameraEventSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCameraEventSummaryStore(): FallbackCameraEventSummaryStore {
    return MemoryFallbackCameraEventSummaryStore(
        mutableMapOf<String, FallbackCameraEventSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCameraEventSummaryStore(
    var inner: FallbackCameraEventSummaryStore,
    var cache: TtlCache<FallbackCameraEventSummary>,
    var hits: Int,
    var misses: Int,
) : FallbackCameraEventSummaryStore {
    override fun find(code: String): FallbackCameraEventSummary? {
        val hit = (cache.get(code) as FallbackCameraEventSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCameraEventSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCameraEventSummary> = inner.list()
}

fun newCachedFallbackCameraEventSummaryStore(inner: FallbackCameraEventSummaryStore, ttl: Int, clock: Clock): FallbackCameraEventSummaryStore {
    return CachedFallbackCameraEventSummaryStore(
        inner = inner,
        cache = newTtlCache<FallbackCameraEventSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback camera event summaries. Empty fields match all. */
data class FallbackCameraEventSummaryQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCameraEventSummary): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<FallbackCameraEventSummary>): MutableList<FallbackCameraEventSummary> {
        val matched = filter(items) { candidate: FallbackCameraEventSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCameraEventSummaries(store: FallbackCameraEventSummaryStore, query: FallbackCameraEventSummaryQuery): MutableList<FallbackCameraEventSummary> {
    return query.apply(store.list())
}
