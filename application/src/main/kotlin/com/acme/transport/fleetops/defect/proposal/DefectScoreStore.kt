package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect scores by code. */
interface DefectScoreStore {
    fun find(code: String): DefectScore?
    fun save(item: DefectScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectScoreStore(
    var items: MutableMap<String, DefectScore>,
    var order: MutableList<String>,
) : DefectScoreStore {
    override fun find(code: String): DefectScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectScore) {
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

    override fun list(): MutableList<DefectScore> {
        val out = mutableListOf<DefectScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectScoreStore(): DefectScoreStore {
    return MemoryDefectScoreStore(mutableMapOf<String, DefectScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectScoreStore(
    var inner: DefectScoreStore,
    var cache: TtlCache<DefectScore>,
    var hits: Int,
    var misses: Int,
) : DefectScoreStore {
    override fun find(code: String): DefectScore? {
        val hit = (cache.get(code) as DefectScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectScore> = inner.list()
}

fun newCachedDefectScoreStore(inner: DefectScoreStore, ttl: Int, clock: Clock): DefectScoreStore {
    return CachedDefectScoreStore(
        inner = inner,
        cache = newTtlCache<DefectScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect scores. Empty fields match all. */
data class DefectScoreQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefectScore>): MutableList<DefectScore> {
        val matched = filter(items) { candidate: DefectScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectScores(store: DefectScoreStore, query: DefectScoreQuery): MutableList<DefectScore> {
    return query.apply(store.list())
}
