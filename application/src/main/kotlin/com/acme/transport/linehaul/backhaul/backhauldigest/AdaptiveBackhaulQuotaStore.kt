package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul quotas by code. */
interface AdaptiveBackhaulQuotaStore {
    fun find(code: String): AdaptiveBackhaulQuota?
    fun save(item: AdaptiveBackhaulQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulQuotaStore(
    var items: MutableMap<String, AdaptiveBackhaulQuota>,
    var order: MutableList<String>,
) : AdaptiveBackhaulQuotaStore {
    override fun find(code: String): AdaptiveBackhaulQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulQuota) {
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

    override fun list(): MutableList<AdaptiveBackhaulQuota> {
        val out = mutableListOf<AdaptiveBackhaulQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulQuotaStore(): AdaptiveBackhaulQuotaStore {
    return MemoryAdaptiveBackhaulQuotaStore(
        mutableMapOf<String, AdaptiveBackhaulQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulQuotaStore(
    var inner: AdaptiveBackhaulQuotaStore,
    var cache: TtlCache<AdaptiveBackhaulQuota>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulQuotaStore {
    override fun find(code: String): AdaptiveBackhaulQuota? {
        val hit = (cache.get(code) as AdaptiveBackhaulQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulQuota> = inner.list()
}

fun newCachedAdaptiveBackhaulQuotaStore(inner: AdaptiveBackhaulQuotaStore, ttl: Int, clock: Clock): AdaptiveBackhaulQuotaStore {
    return CachedAdaptiveBackhaulQuotaStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul quotas. Empty fields match all. */
data class AdaptiveBackhaulQuotaQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveBackhaulQuota>): MutableList<AdaptiveBackhaulQuota> {
        val matched = filter(items) { candidate: AdaptiveBackhaulQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulQuotas(store: AdaptiveBackhaulQuotaStore, query: AdaptiveBackhaulQuotaQuery): MutableList<AdaptiveBackhaulQuota> {
    return query.apply(store.list())
}
