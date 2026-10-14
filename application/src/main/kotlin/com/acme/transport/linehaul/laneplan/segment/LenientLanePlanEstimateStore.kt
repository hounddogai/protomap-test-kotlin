package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient lane plan estimates by code. */
interface LenientLanePlanEstimateStore {
    fun find(code: String): LenientLanePlanEstimate?
    fun save(item: LenientLanePlanEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLanePlanEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLanePlanEstimateStore(
    var items: MutableMap<String, LenientLanePlanEstimate>,
    var order: MutableList<String>,
) : LenientLanePlanEstimateStore {
    override fun find(code: String): LenientLanePlanEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLanePlanEstimate) {
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

    override fun list(): MutableList<LenientLanePlanEstimate> {
        val out = mutableListOf<LenientLanePlanEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLanePlanEstimateStore(): LenientLanePlanEstimateStore {
    return MemoryLenientLanePlanEstimateStore(
        mutableMapOf<String, LenientLanePlanEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLanePlanEstimateStore(
    var inner: LenientLanePlanEstimateStore,
    var cache: TtlCache<LenientLanePlanEstimate>,
    var hits: Int,
    var misses: Int,
) : LenientLanePlanEstimateStore {
    override fun find(code: String): LenientLanePlanEstimate? {
        val hit = (cache.get(code) as LenientLanePlanEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLanePlanEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLanePlanEstimate> = inner.list()
}

fun newCachedLenientLanePlanEstimateStore(inner: LenientLanePlanEstimateStore, ttl: Int, clock: Clock): LenientLanePlanEstimateStore {
    return CachedLenientLanePlanEstimateStore(
        inner = inner,
        cache = newTtlCache<LenientLanePlanEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient lane plan estimates. Empty fields match all. */
data class LenientLanePlanEstimateQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLanePlanEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientLanePlanEstimate>): MutableList<LenientLanePlanEstimate> {
        val matched = filter(items) { candidate: LenientLanePlanEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLanePlanEstimates(store: LenientLanePlanEstimateStore, query: LenientLanePlanEstimateQuery): MutableList<LenientLanePlanEstimate> {
    return query.apply(store.list())
}
