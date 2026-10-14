package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict service area quotas by code. */
interface StrictServiceAreaQuotaStore {
    fun find(code: String): StrictServiceAreaQuota?
    fun save(item: StrictServiceAreaQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictServiceAreaQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictServiceAreaQuotaStore(
    var items: MutableMap<String, StrictServiceAreaQuota>,
    var order: MutableList<String>,
) : StrictServiceAreaQuotaStore {
    override fun find(code: String): StrictServiceAreaQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictServiceAreaQuota) {
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

    override fun list(): MutableList<StrictServiceAreaQuota> {
        val out = mutableListOf<StrictServiceAreaQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictServiceAreaQuotaStore(): StrictServiceAreaQuotaStore {
    return MemoryStrictServiceAreaQuotaStore(
        mutableMapOf<String, StrictServiceAreaQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictServiceAreaQuotaStore(
    var inner: StrictServiceAreaQuotaStore,
    var cache: TtlCache<StrictServiceAreaQuota>,
    var hits: Int,
    var misses: Int,
) : StrictServiceAreaQuotaStore {
    override fun find(code: String): StrictServiceAreaQuota? {
        val hit = (cache.get(code) as StrictServiceAreaQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictServiceAreaQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictServiceAreaQuota> = inner.list()
}

fun newCachedStrictServiceAreaQuotaStore(inner: StrictServiceAreaQuotaStore, ttl: Int, clock: Clock): StrictServiceAreaQuotaStore {
    return CachedStrictServiceAreaQuotaStore(
        inner = inner,
        cache = newTtlCache<StrictServiceAreaQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict service area quotas. Empty fields match all. */
data class StrictServiceAreaQuotaQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictServiceAreaQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictServiceAreaQuota>): MutableList<StrictServiceAreaQuota> {
        val matched = filter(items) { candidate: StrictServiceAreaQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictServiceAreaQuotas(store: StrictServiceAreaQuotaStore, query: StrictServiceAreaQuotaQuery): MutableList<StrictServiceAreaQuota> {
    return query.apply(store.list())
}
