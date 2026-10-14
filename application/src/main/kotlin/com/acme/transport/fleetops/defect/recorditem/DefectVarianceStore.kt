package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect variances by code. */
interface DefectVarianceStore {
    fun find(code: String): DefectVariance?
    fun save(item: DefectVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectVarianceStore(
    var items: MutableMap<String, DefectVariance>,
    var order: MutableList<String>,
) : DefectVarianceStore {
    override fun find(code: String): DefectVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectVariance) {
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

    override fun list(): MutableList<DefectVariance> {
        val out = mutableListOf<DefectVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectVarianceStore(): DefectVarianceStore {
    return MemoryDefectVarianceStore(mutableMapOf<String, DefectVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectVarianceStore(
    var inner: DefectVarianceStore,
    var cache: TtlCache<DefectVariance>,
    var hits: Int,
    var misses: Int,
) : DefectVarianceStore {
    override fun find(code: String): DefectVariance? {
        val hit = (cache.get(code) as DefectVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectVariance> = inner.list()
}

fun newCachedDefectVarianceStore(inner: DefectVarianceStore, ttl: Int, clock: Clock): DefectVarianceStore {
    return CachedDefectVarianceStore(
        inner = inner,
        cache = newTtlCache<DefectVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect variances. Empty fields match all. */
data class DefectVarianceQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectVariance): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectVariance>): MutableList<DefectVariance> {
        val matched = filter(items) { candidate: DefectVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectVariances(store: DefectVarianceStore, query: DefectVarianceQuery): MutableList<DefectVariance> {
    return query.apply(store.list())
}
