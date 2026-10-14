package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict redelivery quotas by code. */
interface StrictRedeliveryQuotaStore {
    fun find(code: String): StrictRedeliveryQuota?
    fun save(item: StrictRedeliveryQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRedeliveryQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRedeliveryQuotaStore(
    var items: MutableMap<String, StrictRedeliveryQuota>,
    var order: MutableList<String>,
) : StrictRedeliveryQuotaStore {
    override fun find(code: String): StrictRedeliveryQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRedeliveryQuota) {
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

    override fun list(): MutableList<StrictRedeliveryQuota> {
        val out = mutableListOf<StrictRedeliveryQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRedeliveryQuotaStore(): StrictRedeliveryQuotaStore {
    return MemoryStrictRedeliveryQuotaStore(
        mutableMapOf<String, StrictRedeliveryQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRedeliveryQuotaStore(
    var inner: StrictRedeliveryQuotaStore,
    var cache: TtlCache<StrictRedeliveryQuota>,
    var hits: Int,
    var misses: Int,
) : StrictRedeliveryQuotaStore {
    override fun find(code: String): StrictRedeliveryQuota? {
        val hit = (cache.get(code) as StrictRedeliveryQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRedeliveryQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRedeliveryQuota> = inner.list()
}

fun newCachedStrictRedeliveryQuotaStore(inner: StrictRedeliveryQuotaStore, ttl: Int, clock: Clock): StrictRedeliveryQuotaStore {
    return CachedStrictRedeliveryQuotaStore(
        inner = inner,
        cache = newTtlCache<StrictRedeliveryQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict redelivery quotas. Empty fields match all. */
data class StrictRedeliveryQuotaQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRedeliveryQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRedeliveryQuota>): MutableList<StrictRedeliveryQuota> {
        val matched = filter(items) { candidate: StrictRedeliveryQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRedeliveryQuotas(store: StrictRedeliveryQuotaStore, query: StrictRedeliveryQuotaQuery): MutableList<StrictRedeliveryQuota> {
    return query.apply(store.list())
}
