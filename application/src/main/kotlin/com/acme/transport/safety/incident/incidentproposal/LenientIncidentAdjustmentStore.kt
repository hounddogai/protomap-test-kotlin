package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient incident adjustments by code. */
interface LenientIncidentAdjustmentStore {
    fun find(code: String): LenientIncidentAdjustment?
    fun save(item: LenientIncidentAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIncidentAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIncidentAdjustmentStore(
    var items: MutableMap<String, LenientIncidentAdjustment>,
    var order: MutableList<String>,
) : LenientIncidentAdjustmentStore {
    override fun find(code: String): LenientIncidentAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIncidentAdjustment) {
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

    override fun list(): MutableList<LenientIncidentAdjustment> {
        val out = mutableListOf<LenientIncidentAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIncidentAdjustmentStore(): LenientIncidentAdjustmentStore {
    return MemoryLenientIncidentAdjustmentStore(
        mutableMapOf<String, LenientIncidentAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIncidentAdjustmentStore(
    var inner: LenientIncidentAdjustmentStore,
    var cache: TtlCache<LenientIncidentAdjustment>,
    var hits: Int,
    var misses: Int,
) : LenientIncidentAdjustmentStore {
    override fun find(code: String): LenientIncidentAdjustment? {
        val hit = (cache.get(code) as LenientIncidentAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIncidentAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIncidentAdjustment> = inner.list()
}

fun newCachedLenientIncidentAdjustmentStore(inner: LenientIncidentAdjustmentStore, ttl: Int, clock: Clock): LenientIncidentAdjustmentStore {
    return CachedLenientIncidentAdjustmentStore(
        inner = inner,
        cache = newTtlCache<LenientIncidentAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient incident adjustments. Empty fields match all. */
data class LenientIncidentAdjustmentQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIncidentAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIncidentAdjustment>): MutableList<LenientIncidentAdjustment> {
        val matched = filter(items) { candidate: LenientIncidentAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIncidentAdjustments(store: LenientIncidentAdjustmentStore, query: LenientIncidentAdjustmentQuery): MutableList<LenientIncidentAdjustment> {
    return query.apply(store.list())
}
