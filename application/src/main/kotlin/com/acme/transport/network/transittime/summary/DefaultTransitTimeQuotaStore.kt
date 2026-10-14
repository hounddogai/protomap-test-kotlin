package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time quotas by code. */
interface DefaultTransitTimeQuotaStore {
    fun find(code: String): DefaultTransitTimeQuota?
    fun save(item: DefaultTransitTimeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimeQuotaStore(
    var items: MutableMap<String, DefaultTransitTimeQuota>,
    var order: MutableList<String>,
) : DefaultTransitTimeQuotaStore {
    override fun find(code: String): DefaultTransitTimeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimeQuota) {
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

    override fun list(): MutableList<DefaultTransitTimeQuota> {
        val out = mutableListOf<DefaultTransitTimeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimeQuotaStore(): DefaultTransitTimeQuotaStore {
    return MemoryDefaultTransitTimeQuotaStore(
        mutableMapOf<String, DefaultTransitTimeQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimeQuotaStore(
    var inner: DefaultTransitTimeQuotaStore,
    var cache: TtlCache<DefaultTransitTimeQuota>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimeQuotaStore {
    override fun find(code: String): DefaultTransitTimeQuota? {
        val hit = (cache.get(code) as DefaultTransitTimeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimeQuota> = inner.list()
}

fun newCachedDefaultTransitTimeQuotaStore(inner: DefaultTransitTimeQuotaStore, ttl: Int, clock: Clock): DefaultTransitTimeQuotaStore {
    return CachedDefaultTransitTimeQuotaStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimeQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time quotas. Empty fields match all. */
data class DefaultTransitTimeQuotaQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimeQuota): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DefaultTransitTimeQuota>): MutableList<DefaultTransitTimeQuota> {
        val matched = filter(items) { candidate: DefaultTransitTimeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimeQuotas(store: DefaultTransitTimeQuotaStore, query: DefaultTransitTimeQuotaQuery): MutableList<DefaultTransitTimeQuota> {
    return query.apply(store.list())
}
