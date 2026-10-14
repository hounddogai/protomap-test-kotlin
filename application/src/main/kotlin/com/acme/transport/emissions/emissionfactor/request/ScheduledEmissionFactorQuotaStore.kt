package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled emission factor quotas by code. */
interface ScheduledEmissionFactorQuotaStore {
    fun find(code: String): ScheduledEmissionFactorQuota?
    fun save(item: ScheduledEmissionFactorQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEmissionFactorQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEmissionFactorQuotaStore(
    var items: MutableMap<String, ScheduledEmissionFactorQuota>,
    var order: MutableList<String>,
) : ScheduledEmissionFactorQuotaStore {
    override fun find(code: String): ScheduledEmissionFactorQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEmissionFactorQuota) {
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

    override fun list(): MutableList<ScheduledEmissionFactorQuota> {
        val out = mutableListOf<ScheduledEmissionFactorQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEmissionFactorQuotaStore(): ScheduledEmissionFactorQuotaStore {
    return MemoryScheduledEmissionFactorQuotaStore(
        mutableMapOf<String, ScheduledEmissionFactorQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEmissionFactorQuotaStore(
    var inner: ScheduledEmissionFactorQuotaStore,
    var cache: TtlCache<ScheduledEmissionFactorQuota>,
    var hits: Int,
    var misses: Int,
) : ScheduledEmissionFactorQuotaStore {
    override fun find(code: String): ScheduledEmissionFactorQuota? {
        val hit = (cache.get(code) as ScheduledEmissionFactorQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEmissionFactorQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEmissionFactorQuota> = inner.list()
}

fun newCachedScheduledEmissionFactorQuotaStore(inner: ScheduledEmissionFactorQuotaStore, ttl: Int, clock: Clock): ScheduledEmissionFactorQuotaStore {
    return CachedScheduledEmissionFactorQuotaStore(
        inner = inner,
        cache = newTtlCache<ScheduledEmissionFactorQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled emission factor quotas. Empty fields match all. */
data class ScheduledEmissionFactorQuotaQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEmissionFactorQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScheduledEmissionFactorQuota>): MutableList<ScheduledEmissionFactorQuota> {
        val matched = filter(items) { candidate: ScheduledEmissionFactorQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEmissionFactorQuotas(store: ScheduledEmissionFactorQuotaStore, query: ScheduledEmissionFactorQuotaQuery): MutableList<ScheduledEmissionFactorQuota> {
    return query.apply(store.list())
}
