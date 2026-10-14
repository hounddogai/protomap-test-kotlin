package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending swap quotas by code. */
interface PendingSwapQuotaStore {
    fun find(code: String): PendingSwapQuota?
    fun save(item: PendingSwapQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSwapQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSwapQuotaStore(
    var items: MutableMap<String, PendingSwapQuota>,
    var order: MutableList<String>,
) : PendingSwapQuotaStore {
    override fun find(code: String): PendingSwapQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSwapQuota) {
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

    override fun list(): MutableList<PendingSwapQuota> {
        val out = mutableListOf<PendingSwapQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSwapQuotaStore(): PendingSwapQuotaStore {
    return MemoryPendingSwapQuotaStore(mutableMapOf<String, PendingSwapQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSwapQuotaStore(
    var inner: PendingSwapQuotaStore,
    var cache: TtlCache<PendingSwapQuota>,
    var hits: Int,
    var misses: Int,
) : PendingSwapQuotaStore {
    override fun find(code: String): PendingSwapQuota? {
        val hit = (cache.get(code) as PendingSwapQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSwapQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSwapQuota> = inner.list()
}

fun newCachedPendingSwapQuotaStore(inner: PendingSwapQuotaStore, ttl: Int, clock: Clock): PendingSwapQuotaStore {
    return CachedPendingSwapQuotaStore(
        inner = inner,
        cache = newTtlCache<PendingSwapQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending swap quotas. Empty fields match all. */
data class PendingSwapQuotaQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSwapQuota): Boolean {
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

    fun apply(items: MutableList<PendingSwapQuota>): MutableList<PendingSwapQuota> {
        val matched = filter(items) { candidate: PendingSwapQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSwapQuotas(store: PendingSwapQuotaStore, query: PendingSwapQuotaQuery): MutableList<PendingSwapQuota> {
    return query.apply(store.list())
}
