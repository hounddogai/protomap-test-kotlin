package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel estimates by code. */
interface ParcelEstimateStore {
    fun find(code: String): ParcelEstimate?
    fun save(item: ParcelEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelEstimateStore(
    var items: MutableMap<String, ParcelEstimate>,
    var order: MutableList<String>,
) : ParcelEstimateStore {
    override fun find(code: String): ParcelEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelEstimate) {
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

    override fun list(): MutableList<ParcelEstimate> {
        val out = mutableListOf<ParcelEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelEstimateStore(): ParcelEstimateStore {
    return MemoryParcelEstimateStore(mutableMapOf<String, ParcelEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelEstimateStore(
    var inner: ParcelEstimateStore,
    var cache: TtlCache<ParcelEstimate>,
    var hits: Int,
    var misses: Int,
) : ParcelEstimateStore {
    override fun find(code: String): ParcelEstimate? {
        val hit = (cache.get(code) as ParcelEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelEstimate> = inner.list()
}

fun newCachedParcelEstimateStore(inner: ParcelEstimateStore, ttl: Int, clock: Clock): ParcelEstimateStore {
    return CachedParcelEstimateStore(
        inner = inner,
        cache = newTtlCache<ParcelEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel estimates. Empty fields match all. */
data class ParcelEstimateQuery(
    var region: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelEstimate>): MutableList<ParcelEstimate> {
        val matched = filter(items) { candidate: ParcelEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelEstimates(store: ParcelEstimateStore, query: ParcelEstimateQuery): MutableList<ParcelEstimate> {
    return query.apply(store.list())
}
