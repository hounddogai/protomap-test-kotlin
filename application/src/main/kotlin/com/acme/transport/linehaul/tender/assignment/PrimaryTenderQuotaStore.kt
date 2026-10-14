package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender quotas by code. */
interface PrimaryTenderQuotaStore {
    fun find(code: String): PrimaryTenderQuota?
    fun save(item: PrimaryTenderQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderQuotaStore(
    var items: MutableMap<String, PrimaryTenderQuota>,
    var order: MutableList<String>,
) : PrimaryTenderQuotaStore {
    override fun find(code: String): PrimaryTenderQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderQuota) {
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

    override fun list(): MutableList<PrimaryTenderQuota> {
        val out = mutableListOf<PrimaryTenderQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderQuotaStore(): PrimaryTenderQuotaStore {
    return MemoryPrimaryTenderQuotaStore(mutableMapOf<String, PrimaryTenderQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderQuotaStore(
    var inner: PrimaryTenderQuotaStore,
    var cache: TtlCache<PrimaryTenderQuota>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderQuotaStore {
    override fun find(code: String): PrimaryTenderQuota? {
        val hit = (cache.get(code) as PrimaryTenderQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderQuota> = inner.list()
}

fun newCachedPrimaryTenderQuotaStore(inner: PrimaryTenderQuotaStore, ttl: Int, clock: Clock): PrimaryTenderQuotaStore {
    return CachedPrimaryTenderQuotaStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender quotas. Empty fields match all. */
data class PrimaryTenderQuotaQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderQuota): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTenderQuota>): MutableList<PrimaryTenderQuota> {
        val matched = filter(items) { candidate: PrimaryTenderQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderQuotas(store: PrimaryTenderQuotaStore, query: PrimaryTenderQuotaQuery): MutableList<PrimaryTenderQuota> {
    return query.apply(store.list())
}
