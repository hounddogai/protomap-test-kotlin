package com.acme.transport.customs.hold.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold quotas by code. */
interface HoldQuotaStore {
    fun find(code: String): HoldQuota?
    fun save(item: HoldQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldQuotaStore(
    var items: MutableMap<String, HoldQuota>,
    var order: MutableList<String>,
) : HoldQuotaStore {
    override fun find(code: String): HoldQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldQuota) {
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

    override fun list(): MutableList<HoldQuota> {
        val out = mutableListOf<HoldQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldQuotaStore(): HoldQuotaStore {
    return MemoryHoldQuotaStore(mutableMapOf<String, HoldQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldQuotaStore(
    var inner: HoldQuotaStore,
    var cache: TtlCache<HoldQuota>,
    var hits: Int,
    var misses: Int,
) : HoldQuotaStore {
    override fun find(code: String): HoldQuota? {
        val hit = (cache.get(code) as HoldQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldQuota> = inner.list()
}

fun newCachedHoldQuotaStore(inner: HoldQuotaStore, ttl: Int, clock: Clock): HoldQuotaStore {
    return CachedHoldQuotaStore(
        inner = inner,
        cache = newTtlCache<HoldQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold quotas. Empty fields match all. */
data class HoldQuotaQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldQuota>): MutableList<HoldQuota> {
        val matched = filter(items) { candidate: HoldQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldQuotas(store: HoldQuotaStore, query: HoldQuotaQuery): MutableList<HoldQuota> {
    return query.apply(store.list())
}
