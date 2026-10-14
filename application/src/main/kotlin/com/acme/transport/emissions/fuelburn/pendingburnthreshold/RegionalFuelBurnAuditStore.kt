package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn audits by code. */
interface RegionalFuelBurnAuditStore {
    fun find(code: String): RegionalFuelBurnAudit?
    fun save(item: RegionalFuelBurnAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnAuditStore(
    var items: MutableMap<String, RegionalFuelBurnAudit>,
    var order: MutableList<String>,
) : RegionalFuelBurnAuditStore {
    override fun find(code: String): RegionalFuelBurnAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnAudit) {
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

    override fun list(): MutableList<RegionalFuelBurnAudit> {
        val out = mutableListOf<RegionalFuelBurnAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnAuditStore(): RegionalFuelBurnAuditStore {
    return MemoryRegionalFuelBurnAuditStore(
        mutableMapOf<String, RegionalFuelBurnAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnAuditStore(
    var inner: RegionalFuelBurnAuditStore,
    var cache: TtlCache<RegionalFuelBurnAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnAuditStore {
    override fun find(code: String): RegionalFuelBurnAudit? {
        val hit = (cache.get(code) as RegionalFuelBurnAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnAudit> = inner.list()
}

fun newCachedRegionalFuelBurnAuditStore(inner: RegionalFuelBurnAuditStore, ttl: Int, clock: Clock): RegionalFuelBurnAuditStore {
    return CachedRegionalFuelBurnAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn audits. Empty fields match all. */
data class RegionalFuelBurnAuditQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RegionalFuelBurnAudit>): MutableList<RegionalFuelBurnAudit> {
        val matched = filter(items) { candidate: RegionalFuelBurnAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnAudits(store: RegionalFuelBurnAuditStore, query: RegionalFuelBurnAuditQuery): MutableList<RegionalFuelBurnAudit> {
    return query.apply(store.list())
}
