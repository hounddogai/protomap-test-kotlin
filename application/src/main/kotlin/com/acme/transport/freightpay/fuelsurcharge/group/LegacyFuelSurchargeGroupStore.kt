package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy fuel surcharge groups by code. */
interface LegacyFuelSurchargeGroupStore {
    fun find(code: String): LegacyFuelSurchargeGroup?
    fun save(item: LegacyFuelSurchargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFuelSurchargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFuelSurchargeGroupStore(
    var items: MutableMap<String, LegacyFuelSurchargeGroup>,
    var order: MutableList<String>,
) : LegacyFuelSurchargeGroupStore {
    override fun find(code: String): LegacyFuelSurchargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFuelSurchargeGroup) {
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

    override fun list(): MutableList<LegacyFuelSurchargeGroup> {
        val out = mutableListOf<LegacyFuelSurchargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFuelSurchargeGroupStore(): LegacyFuelSurchargeGroupStore {
    return MemoryLegacyFuelSurchargeGroupStore(
        mutableMapOf<String, LegacyFuelSurchargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFuelSurchargeGroupStore(
    var inner: LegacyFuelSurchargeGroupStore,
    var cache: TtlCache<LegacyFuelSurchargeGroup>,
    var hits: Int,
    var misses: Int,
) : LegacyFuelSurchargeGroupStore {
    override fun find(code: String): LegacyFuelSurchargeGroup? {
        val hit = (cache.get(code) as LegacyFuelSurchargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFuelSurchargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFuelSurchargeGroup> = inner.list()
}

fun newCachedLegacyFuelSurchargeGroupStore(inner: LegacyFuelSurchargeGroupStore, ttl: Int, clock: Clock): LegacyFuelSurchargeGroupStore {
    return CachedLegacyFuelSurchargeGroupStore(
        inner = inner,
        cache = newTtlCache<LegacyFuelSurchargeGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy fuel surcharge groups. Empty fields match all. */
data class LegacyFuelSurchargeGroupQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFuelSurchargeGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyFuelSurchargeGroup>): MutableList<LegacyFuelSurchargeGroup> {
        val matched = filter(items) { candidate: LegacyFuelSurchargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFuelSurchargeGroups(store: LegacyFuelSurchargeGroupStore, query: LegacyFuelSurchargeGroupQuery): MutableList<LegacyFuelSurchargeGroup> {
    return query.apply(store.list())
}
