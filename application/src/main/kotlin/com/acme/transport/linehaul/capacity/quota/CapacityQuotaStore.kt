package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity quotas by code. */
interface CapacityQuotaStore {
    fun find(code: String): CapacityQuota?
    fun save(item: CapacityQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityQuotaStore(
    var items: MutableMap<String, CapacityQuota>,
    var order: MutableList<String>,
) : CapacityQuotaStore {
    override fun find(code: String): CapacityQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityQuota) {
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

    override fun list(): MutableList<CapacityQuota> {
        val out = mutableListOf<CapacityQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityQuotaStore(): CapacityQuotaStore {
    return MemoryCapacityQuotaStore(mutableMapOf<String, CapacityQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityQuotaStore(
    var inner: CapacityQuotaStore,
    var cache: TtlCache<CapacityQuota>,
    var hits: Int,
    var misses: Int,
) : CapacityQuotaStore {
    override fun find(code: String): CapacityQuota? {
        val hit = (cache.get(code) as CapacityQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityQuota> = inner.list()
}

fun newCachedCapacityQuotaStore(inner: CapacityQuotaStore, ttl: Int, clock: Clock): CapacityQuotaStore {
    return CachedCapacityQuotaStore(
        inner = inner,
        cache = newTtlCache<CapacityQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity quotas. Empty fields match all. */
data class CapacityQuotaQuery(
    var segment: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<CapacityQuota>): MutableList<CapacityQuota> {
        val matched = filter(items) { candidate: CapacityQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityQuotas(store: CapacityQuotaStore, query: CapacityQuotaQuery): MutableList<CapacityQuota> {
    return query.apply(store.list())
}
