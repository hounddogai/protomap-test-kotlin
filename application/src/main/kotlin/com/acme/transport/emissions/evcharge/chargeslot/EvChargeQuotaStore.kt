package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge quotas by code. */
interface EvChargeQuotaStore {
    fun find(code: String): EvChargeQuota?
    fun save(item: EvChargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeQuotaStore(
    var items: MutableMap<String, EvChargeQuota>,
    var order: MutableList<String>,
) : EvChargeQuotaStore {
    override fun find(code: String): EvChargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeQuota) {
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

    override fun list(): MutableList<EvChargeQuota> {
        val out = mutableListOf<EvChargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeQuotaStore(): EvChargeQuotaStore {
    return MemoryEvChargeQuotaStore(mutableMapOf<String, EvChargeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeQuotaStore(
    var inner: EvChargeQuotaStore,
    var cache: TtlCache<EvChargeQuota>,
    var hits: Int,
    var misses: Int,
) : EvChargeQuotaStore {
    override fun find(code: String): EvChargeQuota? {
        val hit = (cache.get(code) as EvChargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeQuota> = inner.list()
}

fun newCachedEvChargeQuotaStore(inner: EvChargeQuotaStore, ttl: Int, clock: Clock): EvChargeQuotaStore {
    return CachedEvChargeQuotaStore(
        inner = inner,
        cache = newTtlCache<EvChargeQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge quotas. Empty fields match all. */
data class EvChargeQuotaQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeQuota>): MutableList<EvChargeQuota> {
        val matched = filter(items) { candidate: EvChargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeQuotas(store: EvChargeQuotaStore, query: EvChargeQuotaQuery): MutableList<EvChargeQuota> {
    return query.apply(store.list())
}
