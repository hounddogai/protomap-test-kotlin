package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan estimates by code. */
interface LanePlanEstimateStore {
    fun find(code: String): LanePlanEstimate?
    fun save(item: LanePlanEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanEstimateStore(
    var items: MutableMap<String, LanePlanEstimate>,
    var order: MutableList<String>,
) : LanePlanEstimateStore {
    override fun find(code: String): LanePlanEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanEstimate) {
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

    override fun list(): MutableList<LanePlanEstimate> {
        val out = mutableListOf<LanePlanEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanEstimateStore(): LanePlanEstimateStore {
    return MemoryLanePlanEstimateStore(mutableMapOf<String, LanePlanEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanEstimateStore(
    var inner: LanePlanEstimateStore,
    var cache: TtlCache<LanePlanEstimate>,
    var hits: Int,
    var misses: Int,
) : LanePlanEstimateStore {
    override fun find(code: String): LanePlanEstimate? {
        val hit = (cache.get(code) as LanePlanEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanEstimate> = inner.list()
}

fun newCachedLanePlanEstimateStore(inner: LanePlanEstimateStore, ttl: Int, clock: Clock): LanePlanEstimateStore {
    return CachedLanePlanEstimateStore(
        inner = inner,
        cache = newTtlCache<LanePlanEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan estimates. Empty fields match all. */
data class LanePlanEstimateQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanEstimate): Boolean {
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

    fun apply(items: MutableList<LanePlanEstimate>): MutableList<LanePlanEstimate> {
        val matched = filter(items) { candidate: LanePlanEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanEstimates(store: LanePlanEstimateStore, query: LanePlanEstimateQuery): MutableList<LanePlanEstimate> {
    return query.apply(store.list())
}
