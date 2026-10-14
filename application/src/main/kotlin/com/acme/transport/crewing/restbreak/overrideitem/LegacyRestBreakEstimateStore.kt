package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rest break estimates by code. */
interface LegacyRestBreakEstimateStore {
    fun find(code: String): LegacyRestBreakEstimate?
    fun save(item: LegacyRestBreakEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRestBreakEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRestBreakEstimateStore(
    var items: MutableMap<String, LegacyRestBreakEstimate>,
    var order: MutableList<String>,
) : LegacyRestBreakEstimateStore {
    override fun find(code: String): LegacyRestBreakEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRestBreakEstimate) {
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

    override fun list(): MutableList<LegacyRestBreakEstimate> {
        val out = mutableListOf<LegacyRestBreakEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRestBreakEstimateStore(): LegacyRestBreakEstimateStore {
    return MemoryLegacyRestBreakEstimateStore(
        mutableMapOf<String, LegacyRestBreakEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRestBreakEstimateStore(
    var inner: LegacyRestBreakEstimateStore,
    var cache: TtlCache<LegacyRestBreakEstimate>,
    var hits: Int,
    var misses: Int,
) : LegacyRestBreakEstimateStore {
    override fun find(code: String): LegacyRestBreakEstimate? {
        val hit = (cache.get(code) as LegacyRestBreakEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRestBreakEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRestBreakEstimate> = inner.list()
}

fun newCachedLegacyRestBreakEstimateStore(inner: LegacyRestBreakEstimateStore, ttl: Int, clock: Clock): LegacyRestBreakEstimateStore {
    return CachedLegacyRestBreakEstimateStore(
        inner = inner,
        cache = newTtlCache<LegacyRestBreakEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rest break estimates. Empty fields match all. */
data class LegacyRestBreakEstimateQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRestBreakEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRestBreakEstimate>): MutableList<LegacyRestBreakEstimate> {
        val matched = filter(items) { candidate: LegacyRestBreakEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRestBreakEstimates(store: LegacyRestBreakEstimateStore, query: LegacyRestBreakEstimateQuery): MutableList<LegacyRestBreakEstimate> {
    return query.apply(store.list())
}
