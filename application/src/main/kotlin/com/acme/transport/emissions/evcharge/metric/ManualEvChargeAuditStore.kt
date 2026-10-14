package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual ev charge audits by code. */
interface ManualEvChargeAuditStore {
    fun find(code: String): ManualEvChargeAudit?
    fun save(item: ManualEvChargeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualEvChargeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualEvChargeAuditStore(
    var items: MutableMap<String, ManualEvChargeAudit>,
    var order: MutableList<String>,
) : ManualEvChargeAuditStore {
    override fun find(code: String): ManualEvChargeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualEvChargeAudit) {
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

    override fun list(): MutableList<ManualEvChargeAudit> {
        val out = mutableListOf<ManualEvChargeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualEvChargeAuditStore(): ManualEvChargeAuditStore {
    return MemoryManualEvChargeAuditStore(mutableMapOf<String, ManualEvChargeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualEvChargeAuditStore(
    var inner: ManualEvChargeAuditStore,
    var cache: TtlCache<ManualEvChargeAudit>,
    var hits: Int,
    var misses: Int,
) : ManualEvChargeAuditStore {
    override fun find(code: String): ManualEvChargeAudit? {
        val hit = (cache.get(code) as ManualEvChargeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualEvChargeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualEvChargeAudit> = inner.list()
}

fun newCachedManualEvChargeAuditStore(inner: ManualEvChargeAuditStore, ttl: Int, clock: Clock): ManualEvChargeAuditStore {
    return CachedManualEvChargeAuditStore(
        inner = inner,
        cache = newTtlCache<ManualEvChargeAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual ev charge audits. Empty fields match all. */
data class ManualEvChargeAuditQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualEvChargeAudit): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ManualEvChargeAudit>): MutableList<ManualEvChargeAudit> {
        val matched = filter(items) { candidate: ManualEvChargeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualEvChargeAudits(store: ManualEvChargeAuditStore, query: ManualEvChargeAuditQuery): MutableList<ManualEvChargeAudit> {
    return query.apply(store.list())
}
