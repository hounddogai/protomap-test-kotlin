package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled swap quotas by code. */
interface ScheduledSwapQuotaStore {
    fun find(code: String): ScheduledSwapQuota?
    fun save(item: ScheduledSwapQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSwapQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSwapQuotaStore(
    var items: MutableMap<String, ScheduledSwapQuota>,
    var order: MutableList<String>,
) : ScheduledSwapQuotaStore {
    override fun find(code: String): ScheduledSwapQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSwapQuota) {
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

    override fun list(): MutableList<ScheduledSwapQuota> {
        val out = mutableListOf<ScheduledSwapQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSwapQuotaStore(): ScheduledSwapQuotaStore {
    return MemoryScheduledSwapQuotaStore(mutableMapOf<String, ScheduledSwapQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSwapQuotaStore(
    var inner: ScheduledSwapQuotaStore,
    var cache: TtlCache<ScheduledSwapQuota>,
    var hits: Int,
    var misses: Int,
) : ScheduledSwapQuotaStore {
    override fun find(code: String): ScheduledSwapQuota? {
        val hit = (cache.get(code) as ScheduledSwapQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSwapQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSwapQuota> = inner.list()
}

fun newCachedScheduledSwapQuotaStore(inner: ScheduledSwapQuotaStore, ttl: Int, clock: Clock): ScheduledSwapQuotaStore {
    return CachedScheduledSwapQuotaStore(
        inner = inner,
        cache = newTtlCache<ScheduledSwapQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled swap quotas. Empty fields match all. */
data class ScheduledSwapQuotaQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSwapQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledSwapQuota>): MutableList<ScheduledSwapQuota> {
        val matched = filter(items) { candidate: ScheduledSwapQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSwapQuotas(store: ScheduledSwapQuotaStore, query: ScheduledSwapQuotaQuery): MutableList<ScheduledSwapQuota> {
    return query.apply(store.list())
}
