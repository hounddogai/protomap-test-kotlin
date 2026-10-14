package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender quotas by code. */
interface TenderQuotaStore {
    fun find(code: String): TenderQuota?
    fun save(item: TenderQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderQuotaStore(
    var items: MutableMap<String, TenderQuota>,
    var order: MutableList<String>,
) : TenderQuotaStore {
    override fun find(code: String): TenderQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderQuota) {
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

    override fun list(): MutableList<TenderQuota> {
        val out = mutableListOf<TenderQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderQuotaStore(): TenderQuotaStore {
    return MemoryTenderQuotaStore(mutableMapOf<String, TenderQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderQuotaStore(
    var inner: TenderQuotaStore,
    var cache: TtlCache<TenderQuota>,
    var hits: Int,
    var misses: Int,
) : TenderQuotaStore {
    override fun find(code: String): TenderQuota? {
        val hit = (cache.get(code) as TenderQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderQuota> = inner.list()
}

fun newCachedTenderQuotaStore(inner: TenderQuotaStore, ttl: Int, clock: Clock): TenderQuotaStore {
    return CachedTenderQuotaStore(
        inner = inner,
        cache = newTtlCache<TenderQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender quotas. Empty fields match all. */
data class TenderQuotaQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TenderQuota>): MutableList<TenderQuota> {
        val matched = filter(items) { candidate: TenderQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderQuotas(store: TenderQuotaStore, query: TenderQuotaQuery): MutableList<TenderQuota> {
    return query.apply(store.list())
}
