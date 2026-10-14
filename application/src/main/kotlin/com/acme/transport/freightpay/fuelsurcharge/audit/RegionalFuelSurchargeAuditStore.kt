package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel surcharge audits by code. */
interface RegionalFuelSurchargeAuditStore {
    fun find(code: String): RegionalFuelSurchargeAudit?
    fun save(item: RegionalFuelSurchargeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelSurchargeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelSurchargeAuditStore(
    var items: MutableMap<String, RegionalFuelSurchargeAudit>,
    var order: MutableList<String>,
) : RegionalFuelSurchargeAuditStore {
    override fun find(code: String): RegionalFuelSurchargeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelSurchargeAudit) {
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

    override fun list(): MutableList<RegionalFuelSurchargeAudit> {
        val out = mutableListOf<RegionalFuelSurchargeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelSurchargeAuditStore(): RegionalFuelSurchargeAuditStore {
    return MemoryRegionalFuelSurchargeAuditStore(
        mutableMapOf<String, RegionalFuelSurchargeAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelSurchargeAuditStore(
    var inner: RegionalFuelSurchargeAuditStore,
    var cache: TtlCache<RegionalFuelSurchargeAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelSurchargeAuditStore {
    override fun find(code: String): RegionalFuelSurchargeAudit? {
        val hit = (cache.get(code) as RegionalFuelSurchargeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelSurchargeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelSurchargeAudit> = inner.list()
}

fun newCachedRegionalFuelSurchargeAuditStore(inner: RegionalFuelSurchargeAuditStore, ttl: Int, clock: Clock): RegionalFuelSurchargeAuditStore {
    return CachedRegionalFuelSurchargeAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelSurchargeAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel surcharge audits. Empty fields match all. */
data class RegionalFuelSurchargeAuditQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelSurchargeAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFuelSurchargeAudit>): MutableList<RegionalFuelSurchargeAudit> {
        val matched = filter(items) { candidate: RegionalFuelSurchargeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelSurchargeAudits(store: RegionalFuelSurchargeAuditStore, query: RegionalFuelSurchargeAuditQuery): MutableList<RegionalFuelSurchargeAudit> {
    return query.apply(store.list())
}
