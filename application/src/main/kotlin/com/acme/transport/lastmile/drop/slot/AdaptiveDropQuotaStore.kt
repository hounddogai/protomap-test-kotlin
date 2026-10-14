package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive drop quotas by code. */
interface AdaptiveDropQuotaStore {
    fun find(code: String): AdaptiveDropQuota?
    fun save(item: AdaptiveDropQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDropQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDropQuotaStore(
    var items: MutableMap<String, AdaptiveDropQuota>,
    var order: MutableList<String>,
) : AdaptiveDropQuotaStore {
    override fun find(code: String): AdaptiveDropQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDropQuota) {
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

    override fun list(): MutableList<AdaptiveDropQuota> {
        val out = mutableListOf<AdaptiveDropQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDropQuotaStore(): AdaptiveDropQuotaStore {
    return MemoryAdaptiveDropQuotaStore(mutableMapOf<String, AdaptiveDropQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDropQuotaStore(
    var inner: AdaptiveDropQuotaStore,
    var cache: TtlCache<AdaptiveDropQuota>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDropQuotaStore {
    override fun find(code: String): AdaptiveDropQuota? {
        val hit = (cache.get(code) as AdaptiveDropQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDropQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDropQuota> = inner.list()
}

fun newCachedAdaptiveDropQuotaStore(inner: AdaptiveDropQuotaStore, ttl: Int, clock: Clock): AdaptiveDropQuotaStore {
    return CachedAdaptiveDropQuotaStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDropQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive drop quotas. Empty fields match all. */
data class AdaptiveDropQuotaQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDropQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDropQuota>): MutableList<AdaptiveDropQuota> {
        val matched = filter(items) { candidate: AdaptiveDropQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDropQuotas(store: AdaptiveDropQuotaStore, query: AdaptiveDropQuotaQuery): MutableList<AdaptiveDropQuota> {
    return query.apply(store.list())
}
