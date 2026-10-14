package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period quotas by code. */
interface DutyPeriodQuotaStore {
    fun find(code: String): DutyPeriodQuota?
    fun save(item: DutyPeriodQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodQuotaStore(
    var items: MutableMap<String, DutyPeriodQuota>,
    var order: MutableList<String>,
) : DutyPeriodQuotaStore {
    override fun find(code: String): DutyPeriodQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodQuota) {
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

    override fun list(): MutableList<DutyPeriodQuota> {
        val out = mutableListOf<DutyPeriodQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodQuotaStore(): DutyPeriodQuotaStore {
    return MemoryDutyPeriodQuotaStore(mutableMapOf<String, DutyPeriodQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodQuotaStore(
    var inner: DutyPeriodQuotaStore,
    var cache: TtlCache<DutyPeriodQuota>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodQuotaStore {
    override fun find(code: String): DutyPeriodQuota? {
        val hit = (cache.get(code) as DutyPeriodQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodQuota> = inner.list()
}

fun newCachedDutyPeriodQuotaStore(inner: DutyPeriodQuotaStore, ttl: Int, clock: Clock): DutyPeriodQuotaStore {
    return CachedDutyPeriodQuotaStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period quotas. Empty fields match all. */
data class DutyPeriodQuotaQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodQuota>): MutableList<DutyPeriodQuota> {
        val matched = filter(items) { candidate: DutyPeriodQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodQuotas(store: DutyPeriodQuotaStore, query: DutyPeriodQuotaQuery): MutableList<DutyPeriodQuota> {
    return query.apply(store.list())
}
