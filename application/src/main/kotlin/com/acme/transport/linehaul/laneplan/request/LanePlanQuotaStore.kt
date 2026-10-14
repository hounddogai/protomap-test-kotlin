package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan quotas by code. */
interface LanePlanQuotaStore {
    fun find(code: String): LanePlanQuota?
    fun save(item: LanePlanQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanQuotaStore(
    var items: MutableMap<String, LanePlanQuota>,
    var order: MutableList<String>,
) : LanePlanQuotaStore {
    override fun find(code: String): LanePlanQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanQuota) {
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

    override fun list(): MutableList<LanePlanQuota> {
        val out = mutableListOf<LanePlanQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanQuotaStore(): LanePlanQuotaStore {
    return MemoryLanePlanQuotaStore(mutableMapOf<String, LanePlanQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanQuotaStore(
    var inner: LanePlanQuotaStore,
    var cache: TtlCache<LanePlanQuota>,
    var hits: Int,
    var misses: Int,
) : LanePlanQuotaStore {
    override fun find(code: String): LanePlanQuota? {
        val hit = (cache.get(code) as LanePlanQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanQuota> = inner.list()
}

fun newCachedLanePlanQuotaStore(inner: LanePlanQuotaStore, ttl: Int, clock: Clock): LanePlanQuotaStore {
    return CachedLanePlanQuotaStore(
        inner = inner,
        cache = newTtlCache<LanePlanQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan quotas. Empty fields match all. */
data class LanePlanQuotaQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanQuota>): MutableList<LanePlanQuota> {
        val matched = filter(items) { candidate: LanePlanQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanQuotas(store: LanePlanQuotaStore, query: LanePlanQuotaQuery): MutableList<LanePlanQuota> {
    return query.apply(store.list())
}
