package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul estimates by code. */
interface BackhaulEstimateStore {
    fun find(code: String): BackhaulEstimate?
    fun save(item: BackhaulEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulEstimateStore(
    var items: MutableMap<String, BackhaulEstimate>,
    var order: MutableList<String>,
) : BackhaulEstimateStore {
    override fun find(code: String): BackhaulEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulEstimate) {
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

    override fun list(): MutableList<BackhaulEstimate> {
        val out = mutableListOf<BackhaulEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulEstimateStore(): BackhaulEstimateStore {
    return MemoryBackhaulEstimateStore(mutableMapOf<String, BackhaulEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulEstimateStore(
    var inner: BackhaulEstimateStore,
    var cache: TtlCache<BackhaulEstimate>,
    var hits: Int,
    var misses: Int,
) : BackhaulEstimateStore {
    override fun find(code: String): BackhaulEstimate? {
        val hit = (cache.get(code) as BackhaulEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulEstimate> = inner.list()
}

fun newCachedBackhaulEstimateStore(inner: BackhaulEstimateStore, ttl: Int, clock: Clock): BackhaulEstimateStore {
    return CachedBackhaulEstimateStore(
        inner = inner,
        cache = newTtlCache<BackhaulEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul estimates. Empty fields match all. */
data class BackhaulEstimateQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulEstimate>): MutableList<BackhaulEstimate> {
        val matched = filter(items) { candidate: BackhaulEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulEstimates(store: BackhaulEstimateStore, query: BackhaulEstimateQuery): MutableList<BackhaulEstimate> {
    return query.apply(store.list())
}
