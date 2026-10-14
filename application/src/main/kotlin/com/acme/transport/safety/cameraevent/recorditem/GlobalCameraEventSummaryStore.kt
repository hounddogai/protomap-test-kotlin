package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global camera event summaries by code. */
interface GlobalCameraEventSummaryStore {
    fun find(code: String): GlobalCameraEventSummary?
    fun save(item: GlobalCameraEventSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCameraEventSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCameraEventSummaryStore(
    var items: MutableMap<String, GlobalCameraEventSummary>,
    var order: MutableList<String>,
) : GlobalCameraEventSummaryStore {
    override fun find(code: String): GlobalCameraEventSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCameraEventSummary) {
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

    override fun list(): MutableList<GlobalCameraEventSummary> {
        val out = mutableListOf<GlobalCameraEventSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCameraEventSummaryStore(): GlobalCameraEventSummaryStore {
    return MemoryGlobalCameraEventSummaryStore(
        mutableMapOf<String, GlobalCameraEventSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCameraEventSummaryStore(
    var inner: GlobalCameraEventSummaryStore,
    var cache: TtlCache<GlobalCameraEventSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalCameraEventSummaryStore {
    override fun find(code: String): GlobalCameraEventSummary? {
        val hit = (cache.get(code) as GlobalCameraEventSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCameraEventSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCameraEventSummary> = inner.list()
}

fun newCachedGlobalCameraEventSummaryStore(inner: GlobalCameraEventSummaryStore, ttl: Int, clock: Clock): GlobalCameraEventSummaryStore {
    return CachedGlobalCameraEventSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalCameraEventSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global camera event summaries. Empty fields match all. */
data class GlobalCameraEventSummaryQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCameraEventSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCameraEventSummary>): MutableList<GlobalCameraEventSummary> {
        val matched = filter(items) { candidate: GlobalCameraEventSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCameraEventSummaries(store: GlobalCameraEventSummaryStore, query: GlobalCameraEventSummaryQuery): MutableList<GlobalCameraEventSummary> {
    return query.apply(store.list())
}
