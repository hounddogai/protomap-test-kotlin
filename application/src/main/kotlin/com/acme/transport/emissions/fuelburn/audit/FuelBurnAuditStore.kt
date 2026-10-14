package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn audits by code. */
interface FuelBurnAuditStore {
    fun find(code: String): FuelBurnAudit?
    fun save(item: FuelBurnAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnAuditStore(
    var items: MutableMap<String, FuelBurnAudit>,
    var order: MutableList<String>,
) : FuelBurnAuditStore {
    override fun find(code: String): FuelBurnAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnAudit) {
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

    override fun list(): MutableList<FuelBurnAudit> {
        val out = mutableListOf<FuelBurnAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnAuditStore(): FuelBurnAuditStore {
    return MemoryFuelBurnAuditStore(mutableMapOf<String, FuelBurnAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnAuditStore(
    var inner: FuelBurnAuditStore,
    var cache: TtlCache<FuelBurnAudit>,
    var hits: Int,
    var misses: Int,
) : FuelBurnAuditStore {
    override fun find(code: String): FuelBurnAudit? {
        val hit = (cache.get(code) as FuelBurnAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnAudit> = inner.list()
}

fun newCachedFuelBurnAuditStore(inner: FuelBurnAuditStore, ttl: Int, clock: Clock): FuelBurnAuditStore {
    return CachedFuelBurnAuditStore(
        inner = inner,
        cache = newTtlCache<FuelBurnAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn audits. Empty fields match all. */
data class FuelBurnAuditQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnAudit>): MutableList<FuelBurnAudit> {
        val matched = filter(items) { candidate: FuelBurnAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnAudits(store: FuelBurnAuditStore, query: FuelBurnAuditQuery): MutableList<FuelBurnAudit> {
    return query.apply(store.list())
}
