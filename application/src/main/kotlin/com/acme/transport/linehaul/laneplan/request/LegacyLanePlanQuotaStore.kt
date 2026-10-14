package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy lane plan quotas by code. */
interface LegacyLanePlanQuotaStore {
    fun find(code: String): LegacyLanePlanQuota?
    fun save(item: LegacyLanePlanQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLanePlanQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLanePlanQuotaStore(
    var items: MutableMap<String, LegacyLanePlanQuota>,
    var order: MutableList<String>,
) : LegacyLanePlanQuotaStore {
    override fun find(code: String): LegacyLanePlanQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLanePlanQuota) {
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

    override fun list(): MutableList<LegacyLanePlanQuota> {
        val out = mutableListOf<LegacyLanePlanQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLanePlanQuotaStore(): LegacyLanePlanQuotaStore {
    return MemoryLegacyLanePlanQuotaStore(mutableMapOf<String, LegacyLanePlanQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLanePlanQuotaStore(
    var inner: LegacyLanePlanQuotaStore,
    var cache: TtlCache<LegacyLanePlanQuota>,
    var hits: Int,
    var misses: Int,
) : LegacyLanePlanQuotaStore {
    override fun find(code: String): LegacyLanePlanQuota? {
        val hit = (cache.get(code) as LegacyLanePlanQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLanePlanQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLanePlanQuota> = inner.list()
}

fun newCachedLegacyLanePlanQuotaStore(inner: LegacyLanePlanQuotaStore, ttl: Int, clock: Clock): LegacyLanePlanQuotaStore {
    return CachedLegacyLanePlanQuotaStore(
        inner = inner,
        cache = newTtlCache<LegacyLanePlanQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lane plan quotas. Empty fields match all. */
data class LegacyLanePlanQuotaQuery(
    var segment: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLanePlanQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLanePlanQuota>): MutableList<LegacyLanePlanQuota> {
        val matched = filter(items) { candidate: LegacyLanePlanQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLanePlanQuotas(store: LegacyLanePlanQuotaStore, query: LegacyLanePlanQuotaQuery): MutableList<LegacyLanePlanQuota> {
    return query.apply(store.list())
}
