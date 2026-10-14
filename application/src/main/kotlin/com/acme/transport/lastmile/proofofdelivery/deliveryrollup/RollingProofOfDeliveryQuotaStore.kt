package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery quotas by code. */
interface RollingProofOfDeliveryQuotaStore {
    fun find(code: String): RollingProofOfDeliveryQuota?
    fun save(item: RollingProofOfDeliveryQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryQuotaStore(
    var items: MutableMap<String, RollingProofOfDeliveryQuota>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryQuotaStore {
    override fun find(code: String): RollingProofOfDeliveryQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryQuota) {
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

    override fun list(): MutableList<RollingProofOfDeliveryQuota> {
        val out = mutableListOf<RollingProofOfDeliveryQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryQuotaStore(): RollingProofOfDeliveryQuotaStore {
    return MemoryRollingProofOfDeliveryQuotaStore(
        mutableMapOf<String, RollingProofOfDeliveryQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryQuotaStore(
    var inner: RollingProofOfDeliveryQuotaStore,
    var cache: TtlCache<RollingProofOfDeliveryQuota>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryQuotaStore {
    override fun find(code: String): RollingProofOfDeliveryQuota? {
        val hit = (cache.get(code) as RollingProofOfDeliveryQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryQuota> = inner.list()
}

fun newCachedRollingProofOfDeliveryQuotaStore(inner: RollingProofOfDeliveryQuotaStore, ttl: Int, clock: Clock): RollingProofOfDeliveryQuotaStore {
    return CachedRollingProofOfDeliveryQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery quotas. Empty fields match all. */
data class RollingProofOfDeliveryQuotaQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingProofOfDeliveryQuota>): MutableList<RollingProofOfDeliveryQuota> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryQuotas(store: RollingProofOfDeliveryQuotaStore, query: RollingProofOfDeliveryQuotaQuery): MutableList<RollingProofOfDeliveryQuota> {
    return query.apply(store.list())
}
