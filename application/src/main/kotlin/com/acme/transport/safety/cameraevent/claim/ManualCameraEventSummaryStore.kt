package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event summaries by code. */
interface ManualCameraEventSummaryStore {
    fun find(code: String): ManualCameraEventSummary?
    fun save(item: ManualCameraEventSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventSummaryStore(
    var items: MutableMap<String, ManualCameraEventSummary>,
    var order: MutableList<String>,
) : ManualCameraEventSummaryStore {
    override fun find(code: String): ManualCameraEventSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventSummary) {
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

    override fun list(): MutableList<ManualCameraEventSummary> {
        val out = mutableListOf<ManualCameraEventSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventSummaryStore(): ManualCameraEventSummaryStore {
    return MemoryManualCameraEventSummaryStore(
        mutableMapOf<String, ManualCameraEventSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventSummaryStore(
    var inner: ManualCameraEventSummaryStore,
    var cache: TtlCache<ManualCameraEventSummary>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventSummaryStore {
    override fun find(code: String): ManualCameraEventSummary? {
        val hit = (cache.get(code) as ManualCameraEventSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventSummary> = inner.list()
}

fun newCachedManualCameraEventSummaryStore(inner: ManualCameraEventSummaryStore, ttl: Int, clock: Clock): ManualCameraEventSummaryStore {
    return CachedManualCameraEventSummaryStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event summaries. Empty fields match all. */
data class ManualCameraEventSummaryQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventSummary>): MutableList<ManualCameraEventSummary> {
        val matched = filter(items) { candidate: ManualCameraEventSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventSummaries(store: ManualCameraEventSummaryStore, query: ManualCameraEventSummaryQuery): MutableList<ManualCameraEventSummary> {
    return query.apply(store.list())
}
