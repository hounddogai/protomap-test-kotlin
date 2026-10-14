package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery quotas by code. */
interface RedeliveryQuotaStore {
    fun find(code: String): RedeliveryQuota?
    fun save(item: RedeliveryQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryQuotaStore(
    var items: MutableMap<String, RedeliveryQuota>,
    var order: MutableList<String>,
) : RedeliveryQuotaStore {
    override fun find(code: String): RedeliveryQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryQuota) {
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

    override fun list(): MutableList<RedeliveryQuota> {
        val out = mutableListOf<RedeliveryQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryQuotaStore(): RedeliveryQuotaStore {
    return MemoryRedeliveryQuotaStore(mutableMapOf<String, RedeliveryQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryQuotaStore(
    var inner: RedeliveryQuotaStore,
    var cache: TtlCache<RedeliveryQuota>,
    var hits: Int,
    var misses: Int,
) : RedeliveryQuotaStore {
    override fun find(code: String): RedeliveryQuota? {
        val hit = (cache.get(code) as RedeliveryQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryQuota> = inner.list()
}

fun newCachedRedeliveryQuotaStore(inner: RedeliveryQuotaStore, ttl: Int, clock: Clock): RedeliveryQuotaStore {
    return CachedRedeliveryQuotaStore(
        inner = inner,
        cache = newTtlCache<RedeliveryQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery quotas. Empty fields match all. */
data class RedeliveryQuotaQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryQuota>): MutableList<RedeliveryQuota> {
        val matched = filter(items) { candidate: RedeliveryQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryQuotas(store: RedeliveryQuotaStore, query: RedeliveryQuotaQuery): MutableList<RedeliveryQuota> {
    return query.apply(store.list())
}
