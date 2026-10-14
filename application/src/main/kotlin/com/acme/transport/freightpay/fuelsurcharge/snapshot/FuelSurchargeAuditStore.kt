package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge audits by code. */
interface FuelSurchargeAuditStore {
    fun find(code: String): FuelSurchargeAudit?
    fun save(item: FuelSurchargeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeAuditStore(
    var items: MutableMap<String, FuelSurchargeAudit>,
    var order: MutableList<String>,
) : FuelSurchargeAuditStore {
    override fun find(code: String): FuelSurchargeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeAudit) {
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

    override fun list(): MutableList<FuelSurchargeAudit> {
        val out = mutableListOf<FuelSurchargeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeAuditStore(): FuelSurchargeAuditStore {
    return MemoryFuelSurchargeAuditStore(mutableMapOf<String, FuelSurchargeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeAuditStore(
    var inner: FuelSurchargeAuditStore,
    var cache: TtlCache<FuelSurchargeAudit>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeAuditStore {
    override fun find(code: String): FuelSurchargeAudit? {
        val hit = (cache.get(code) as FuelSurchargeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeAudit> = inner.list()
}

fun newCachedFuelSurchargeAuditStore(inner: FuelSurchargeAuditStore, ttl: Int, clock: Clock): FuelSurchargeAuditStore {
    return CachedFuelSurchargeAuditStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge audits. Empty fields match all. */
data class FuelSurchargeAuditQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeAudit): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<FuelSurchargeAudit>): MutableList<FuelSurchargeAudit> {
        val matched = filter(items) { candidate: FuelSurchargeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeAudits(store: FuelSurchargeAuditStore, query: FuelSurchargeAuditQuery): MutableList<FuelSurchargeAudit> {
    return query.apply(store.list())
}
