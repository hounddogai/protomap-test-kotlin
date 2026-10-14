package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss quotas by code. */
interface ScheduledNearMissQuotaStore {
    fun find(code: String): ScheduledNearMissQuota?
    fun save(item: ScheduledNearMissQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissQuotaStore(
    var items: MutableMap<String, ScheduledNearMissQuota>,
    var order: MutableList<String>,
) : ScheduledNearMissQuotaStore {
    override fun find(code: String): ScheduledNearMissQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissQuota) {
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

    override fun list(): MutableList<ScheduledNearMissQuota> {
        val out = mutableListOf<ScheduledNearMissQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissQuotaStore(): ScheduledNearMissQuotaStore {
    return MemoryScheduledNearMissQuotaStore(
        mutableMapOf<String, ScheduledNearMissQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissQuotaStore(
    var inner: ScheduledNearMissQuotaStore,
    var cache: TtlCache<ScheduledNearMissQuota>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissQuotaStore {
    override fun find(code: String): ScheduledNearMissQuota? {
        val hit = (cache.get(code) as ScheduledNearMissQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissQuota> = inner.list()
}

fun newCachedScheduledNearMissQuotaStore(inner: ScheduledNearMissQuotaStore, ttl: Int, clock: Clock): ScheduledNearMissQuotaStore {
    return CachedScheduledNearMissQuotaStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss quotas. Empty fields match all. */
data class ScheduledNearMissQuotaQuery(
    var region: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNearMissQuota>): MutableList<ScheduledNearMissQuota> {
        val matched = filter(items) { candidate: ScheduledNearMissQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissQuotas(store: ScheduledNearMissQuotaStore, query: ScheduledNearMissQuotaQuery): MutableList<ScheduledNearMissQuota> {
    return query.apply(store.list())
}
