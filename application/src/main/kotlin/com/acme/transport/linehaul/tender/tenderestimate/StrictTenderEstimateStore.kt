package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict tender estimates by code. */
interface StrictTenderEstimateStore {
    fun find(code: String): StrictTenderEstimate?
    fun save(item: StrictTenderEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTenderEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTenderEstimateStore(
    var items: MutableMap<String, StrictTenderEstimate>,
    var order: MutableList<String>,
) : StrictTenderEstimateStore {
    override fun find(code: String): StrictTenderEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTenderEstimate) {
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

    override fun list(): MutableList<StrictTenderEstimate> {
        val out = mutableListOf<StrictTenderEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTenderEstimateStore(): StrictTenderEstimateStore {
    return MemoryStrictTenderEstimateStore(
        mutableMapOf<String, StrictTenderEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTenderEstimateStore(
    var inner: StrictTenderEstimateStore,
    var cache: TtlCache<StrictTenderEstimate>,
    var hits: Int,
    var misses: Int,
) : StrictTenderEstimateStore {
    override fun find(code: String): StrictTenderEstimate? {
        val hit = (cache.get(code) as StrictTenderEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTenderEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTenderEstimate> = inner.list()
}

fun newCachedStrictTenderEstimateStore(inner: StrictTenderEstimateStore, ttl: Int, clock: Clock): StrictTenderEstimateStore {
    return CachedStrictTenderEstimateStore(
        inner = inner,
        cache = newTtlCache<StrictTenderEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tender estimates. Empty fields match all. */
data class StrictTenderEstimateQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTenderEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTenderEstimate>): MutableList<StrictTenderEstimate> {
        val matched = filter(items) { candidate: StrictTenderEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTenderEstimates(store: StrictTenderEstimateStore, query: StrictTenderEstimateQuery): MutableList<StrictTenderEstimate> {
    return query.apply(store.list())
}
