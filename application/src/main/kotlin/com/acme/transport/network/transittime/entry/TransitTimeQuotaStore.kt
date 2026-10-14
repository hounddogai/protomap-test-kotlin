package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time quotas by code. */
interface TransitTimeQuotaStore {
    fun find(code: String): TransitTimeQuota?
    fun save(item: TransitTimeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeQuotaStore(
    var items: MutableMap<String, TransitTimeQuota>,
    var order: MutableList<String>,
) : TransitTimeQuotaStore {
    override fun find(code: String): TransitTimeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeQuota) {
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

    override fun list(): MutableList<TransitTimeQuota> {
        val out = mutableListOf<TransitTimeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeQuotaStore(): TransitTimeQuotaStore {
    return MemoryTransitTimeQuotaStore(mutableMapOf<String, TransitTimeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeQuotaStore(
    var inner: TransitTimeQuotaStore,
    var cache: TtlCache<TransitTimeQuota>,
    var hits: Int,
    var misses: Int,
) : TransitTimeQuotaStore {
    override fun find(code: String): TransitTimeQuota? {
        val hit = (cache.get(code) as TransitTimeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeQuota> = inner.list()
}

fun newCachedTransitTimeQuotaStore(inner: TransitTimeQuotaStore, ttl: Int, clock: Clock): TransitTimeQuotaStore {
    return CachedTransitTimeQuotaStore(
        inner = inner,
        cache = newTtlCache<TransitTimeQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time quotas. Empty fields match all. */
data class TransitTimeQuotaQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeQuota>): MutableList<TransitTimeQuota> {
        val matched = filter(items) { candidate: TransitTimeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeQuotas(store: TransitTimeQuotaStore, query: TransitTimeQuotaQuery): MutableList<TransitTimeQuota> {
    return query.apply(store.list())
}
