package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident estimates by code. */
interface IncidentEstimateStore {
    fun find(code: String): IncidentEstimate?
    fun save(item: IncidentEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentEstimateStore(
    var items: MutableMap<String, IncidentEstimate>,
    var order: MutableList<String>,
) : IncidentEstimateStore {
    override fun find(code: String): IncidentEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentEstimate) {
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

    override fun list(): MutableList<IncidentEstimate> {
        val out = mutableListOf<IncidentEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentEstimateStore(): IncidentEstimateStore {
    return MemoryIncidentEstimateStore(mutableMapOf<String, IncidentEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentEstimateStore(
    var inner: IncidentEstimateStore,
    var cache: TtlCache<IncidentEstimate>,
    var hits: Int,
    var misses: Int,
) : IncidentEstimateStore {
    override fun find(code: String): IncidentEstimate? {
        val hit = (cache.get(code) as IncidentEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentEstimate> = inner.list()
}

fun newCachedIncidentEstimateStore(inner: IncidentEstimateStore, ttl: Int, clock: Clock): IncidentEstimateStore {
    return CachedIncidentEstimateStore(
        inner = inner,
        cache = newTtlCache<IncidentEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident estimates. Empty fields match all. */
data class IncidentEstimateQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentEstimate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentEstimate>): MutableList<IncidentEstimate> {
        val matched = filter(items) { candidate: IncidentEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentEstimates(store: IncidentEstimateStore, query: IncidentEstimateQuery): MutableList<IncidentEstimate> {
    return query.apply(store.list())
}
