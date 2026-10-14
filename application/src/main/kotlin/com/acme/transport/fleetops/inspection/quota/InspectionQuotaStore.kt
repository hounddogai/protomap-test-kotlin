package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection quotas by code. */
interface InspectionQuotaStore {
    fun find(code: String): InspectionQuota?
    fun save(item: InspectionQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionQuotaStore(
    var items: MutableMap<String, InspectionQuota>,
    var order: MutableList<String>,
) : InspectionQuotaStore {
    override fun find(code: String): InspectionQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionQuota) {
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

    override fun list(): MutableList<InspectionQuota> {
        val out = mutableListOf<InspectionQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionQuotaStore(): InspectionQuotaStore {
    return MemoryInspectionQuotaStore(mutableMapOf<String, InspectionQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionQuotaStore(
    var inner: InspectionQuotaStore,
    var cache: TtlCache<InspectionQuota>,
    var hits: Int,
    var misses: Int,
) : InspectionQuotaStore {
    override fun find(code: String): InspectionQuota? {
        val hit = (cache.get(code) as InspectionQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionQuota> = inner.list()
}

fun newCachedInspectionQuotaStore(inner: InspectionQuotaStore, ttl: Int, clock: Clock): InspectionQuotaStore {
    return CachedInspectionQuotaStore(
        inner = inner,
        cache = newTtlCache<InspectionQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection quotas. Empty fields match all. */
data class InspectionQuotaQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<InspectionQuota>): MutableList<InspectionQuota> {
        val matched = filter(items) { candidate: InspectionQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionQuotas(store: InspectionQuotaStore, query: InspectionQuotaQuery): MutableList<InspectionQuota> {
    return query.apply(store.list())
}
