package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual locker quotas by code. */
interface ManualLockerQuotaStore {
    fun find(code: String): ManualLockerQuota?
    fun save(item: ManualLockerQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLockerQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLockerQuotaStore(
    var items: MutableMap<String, ManualLockerQuota>,
    var order: MutableList<String>,
) : ManualLockerQuotaStore {
    override fun find(code: String): ManualLockerQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLockerQuota) {
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

    override fun list(): MutableList<ManualLockerQuota> {
        val out = mutableListOf<ManualLockerQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLockerQuotaStore(): ManualLockerQuotaStore {
    return MemoryManualLockerQuotaStore(mutableMapOf<String, ManualLockerQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLockerQuotaStore(
    var inner: ManualLockerQuotaStore,
    var cache: TtlCache<ManualLockerQuota>,
    var hits: Int,
    var misses: Int,
) : ManualLockerQuotaStore {
    override fun find(code: String): ManualLockerQuota? {
        val hit = (cache.get(code) as ManualLockerQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLockerQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLockerQuota> = inner.list()
}

fun newCachedManualLockerQuotaStore(inner: ManualLockerQuotaStore, ttl: Int, clock: Clock): ManualLockerQuotaStore {
    return CachedManualLockerQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualLockerQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual locker quotas. Empty fields match all. */
data class ManualLockerQuotaQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLockerQuota): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLockerQuota>): MutableList<ManualLockerQuota> {
        val matched = filter(items) { candidate: ManualLockerQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLockerQuotas(store: ManualLockerQuotaStore, query: ManualLockerQuotaQuery): MutableList<ManualLockerQuota> {
    return query.apply(store.list())
}
