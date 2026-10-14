package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss estimates by code. */
interface NearMissEstimateStore {
    fun find(code: String): NearMissEstimate?
    fun save(item: NearMissEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissEstimateStore(
    var items: MutableMap<String, NearMissEstimate>,
    var order: MutableList<String>,
) : NearMissEstimateStore {
    override fun find(code: String): NearMissEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissEstimate) {
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

    override fun list(): MutableList<NearMissEstimate> {
        val out = mutableListOf<NearMissEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissEstimateStore(): NearMissEstimateStore {
    return MemoryNearMissEstimateStore(mutableMapOf<String, NearMissEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissEstimateStore(
    var inner: NearMissEstimateStore,
    var cache: TtlCache<NearMissEstimate>,
    var hits: Int,
    var misses: Int,
) : NearMissEstimateStore {
    override fun find(code: String): NearMissEstimate? {
        val hit = (cache.get(code) as NearMissEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissEstimate> = inner.list()
}

fun newCachedNearMissEstimateStore(inner: NearMissEstimateStore, ttl: Int, clock: Clock): NearMissEstimateStore {
    return CachedNearMissEstimateStore(
        inner = inner,
        cache = newTtlCache<NearMissEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss estimates. Empty fields match all. */
data class NearMissEstimateQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<NearMissEstimate>): MutableList<NearMissEstimate> {
        val matched = filter(items) { candidate: NearMissEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissEstimates(store: NearMissEstimateStore, query: NearMissEstimateQuery): MutableList<NearMissEstimate> {
    return query.apply(store.list())
}
