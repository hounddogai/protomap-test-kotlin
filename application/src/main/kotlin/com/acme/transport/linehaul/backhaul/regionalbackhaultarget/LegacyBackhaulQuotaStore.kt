package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul quotas by code. */
interface LegacyBackhaulQuotaStore {
    fun find(code: String): LegacyBackhaulQuota?
    fun save(item: LegacyBackhaulQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulQuotaStore(
    var items: MutableMap<String, LegacyBackhaulQuota>,
    var order: MutableList<String>,
) : LegacyBackhaulQuotaStore {
    override fun find(code: String): LegacyBackhaulQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulQuota) {
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

    override fun list(): MutableList<LegacyBackhaulQuota> {
        val out = mutableListOf<LegacyBackhaulQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulQuotaStore(): LegacyBackhaulQuotaStore {
    return MemoryLegacyBackhaulQuotaStore(mutableMapOf<String, LegacyBackhaulQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulQuotaStore(
    var inner: LegacyBackhaulQuotaStore,
    var cache: TtlCache<LegacyBackhaulQuota>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulQuotaStore {
    override fun find(code: String): LegacyBackhaulQuota? {
        val hit = (cache.get(code) as LegacyBackhaulQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulQuota> = inner.list()
}

fun newCachedLegacyBackhaulQuotaStore(inner: LegacyBackhaulQuotaStore, ttl: Int, clock: Clock): LegacyBackhaulQuotaStore {
    return CachedLegacyBackhaulQuotaStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul quotas. Empty fields match all. */
data class LegacyBackhaulQuotaQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulQuota>): MutableList<LegacyBackhaulQuota> {
        val matched = filter(items) { candidate: LegacyBackhaulQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulQuotas(store: LegacyBackhaulQuotaStore, query: LegacyBackhaulQuotaQuery): MutableList<LegacyBackhaulQuota> {
    return query.apply(store.list())
}
