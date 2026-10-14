package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect quotas by code. */
interface DefectQuotaStore {
    fun find(code: String): DefectQuota?
    fun save(item: DefectQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectQuotaStore(
    var items: MutableMap<String, DefectQuota>,
    var order: MutableList<String>,
) : DefectQuotaStore {
    override fun find(code: String): DefectQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectQuota) {
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

    override fun list(): MutableList<DefectQuota> {
        val out = mutableListOf<DefectQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectQuotaStore(): DefectQuotaStore {
    return MemoryDefectQuotaStore(mutableMapOf<String, DefectQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectQuotaStore(
    var inner: DefectQuotaStore,
    var cache: TtlCache<DefectQuota>,
    var hits: Int,
    var misses: Int,
) : DefectQuotaStore {
    override fun find(code: String): DefectQuota? {
        val hit = (cache.get(code) as DefectQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectQuota> = inner.list()
}

fun newCachedDefectQuotaStore(inner: DefectQuotaStore, ttl: Int, clock: Clock): DefectQuotaStore {
    return CachedDefectQuotaStore(
        inner = inner,
        cache = newTtlCache<DefectQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect quotas. Empty fields match all. */
data class DefectQuotaQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectQuota>): MutableList<DefectQuota> {
        val matched = filter(items) { candidate: DefectQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectQuotas(store: DefectQuotaStore, query: DefectQuotaQuery): MutableList<DefectQuota> {
    return query.apply(store.list())
}
