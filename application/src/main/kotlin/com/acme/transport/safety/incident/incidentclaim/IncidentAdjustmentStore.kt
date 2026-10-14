package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident adjustments by code. */
interface IncidentAdjustmentStore {
    fun find(code: String): IncidentAdjustment?
    fun save(item: IncidentAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentAdjustmentStore(
    var items: MutableMap<String, IncidentAdjustment>,
    var order: MutableList<String>,
) : IncidentAdjustmentStore {
    override fun find(code: String): IncidentAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentAdjustment) {
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

    override fun list(): MutableList<IncidentAdjustment> {
        val out = mutableListOf<IncidentAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentAdjustmentStore(): IncidentAdjustmentStore {
    return MemoryIncidentAdjustmentStore(mutableMapOf<String, IncidentAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentAdjustmentStore(
    var inner: IncidentAdjustmentStore,
    var cache: TtlCache<IncidentAdjustment>,
    var hits: Int,
    var misses: Int,
) : IncidentAdjustmentStore {
    override fun find(code: String): IncidentAdjustment? {
        val hit = (cache.get(code) as IncidentAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentAdjustment> = inner.list()
}

fun newCachedIncidentAdjustmentStore(inner: IncidentAdjustmentStore, ttl: Int, clock: Clock): IncidentAdjustmentStore {
    return CachedIncidentAdjustmentStore(
        inner = inner,
        cache = newTtlCache<IncidentAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident adjustments. Empty fields match all. */
data class IncidentAdjustmentQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<IncidentAdjustment>): MutableList<IncidentAdjustment> {
        val matched = filter(items) { candidate: IncidentAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentAdjustments(store: IncidentAdjustmentStore, query: IncidentAdjustmentQuery): MutableList<IncidentAdjustment> {
    return query.apply(store.list())
}
