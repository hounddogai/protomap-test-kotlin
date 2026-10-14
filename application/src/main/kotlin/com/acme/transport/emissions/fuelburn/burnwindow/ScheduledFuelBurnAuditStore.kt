package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel burn audits by code. */
interface ScheduledFuelBurnAuditStore {
    fun find(code: String): ScheduledFuelBurnAudit?
    fun save(item: ScheduledFuelBurnAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelBurnAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelBurnAuditStore(
    var items: MutableMap<String, ScheduledFuelBurnAudit>,
    var order: MutableList<String>,
) : ScheduledFuelBurnAuditStore {
    override fun find(code: String): ScheduledFuelBurnAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelBurnAudit) {
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

    override fun list(): MutableList<ScheduledFuelBurnAudit> {
        val out = mutableListOf<ScheduledFuelBurnAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelBurnAuditStore(): ScheduledFuelBurnAuditStore {
    return MemoryScheduledFuelBurnAuditStore(
        mutableMapOf<String, ScheduledFuelBurnAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelBurnAuditStore(
    var inner: ScheduledFuelBurnAuditStore,
    var cache: TtlCache<ScheduledFuelBurnAudit>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelBurnAuditStore {
    override fun find(code: String): ScheduledFuelBurnAudit? {
        val hit = (cache.get(code) as ScheduledFuelBurnAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelBurnAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelBurnAudit> = inner.list()
}

fun newCachedScheduledFuelBurnAuditStore(inner: ScheduledFuelBurnAuditStore, ttl: Int, clock: Clock): ScheduledFuelBurnAuditStore {
    return CachedScheduledFuelBurnAuditStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelBurnAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel burn audits. Empty fields match all. */
data class ScheduledFuelBurnAuditQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelBurnAudit): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelBurnAudit>): MutableList<ScheduledFuelBurnAudit> {
        val matched = filter(items) { candidate: ScheduledFuelBurnAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelBurnAudits(store: ScheduledFuelBurnAuditStore, query: ScheduledFuelBurnAuditQuery): MutableList<ScheduledFuelBurnAudit> {
    return query.apply(store.list())
}
