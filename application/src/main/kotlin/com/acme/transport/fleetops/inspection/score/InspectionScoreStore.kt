package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection scores by code. */
interface InspectionScoreStore {
    fun find(code: String): InspectionScore?
    fun save(item: InspectionScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionScoreStore(
    var items: MutableMap<String, InspectionScore>,
    var order: MutableList<String>,
) : InspectionScoreStore {
    override fun find(code: String): InspectionScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionScore) {
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

    override fun list(): MutableList<InspectionScore> {
        val out = mutableListOf<InspectionScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionScoreStore(): InspectionScoreStore {
    return MemoryInspectionScoreStore(mutableMapOf<String, InspectionScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionScoreStore(
    var inner: InspectionScoreStore,
    var cache: TtlCache<InspectionScore>,
    var hits: Int,
    var misses: Int,
) : InspectionScoreStore {
    override fun find(code: String): InspectionScore? {
        val hit = (cache.get(code) as InspectionScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionScore> = inner.list()
}

fun newCachedInspectionScoreStore(inner: InspectionScoreStore, ttl: Int, clock: Clock): InspectionScoreStore {
    return CachedInspectionScoreStore(
        inner = inner,
        cache = newTtlCache<InspectionScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection scores. Empty fields match all. */
data class InspectionScoreQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionScore>): MutableList<InspectionScore> {
        val matched = filter(items) { candidate: InspectionScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionScores(store: InspectionScoreStore, query: InspectionScoreQuery): MutableList<InspectionScore> {
    return query.apply(store.list())
}
