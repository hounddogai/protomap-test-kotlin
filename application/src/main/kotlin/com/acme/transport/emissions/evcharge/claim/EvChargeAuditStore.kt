package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge audits by code. */
interface EvChargeAuditStore {
    fun find(code: String): EvChargeAudit?
    fun save(item: EvChargeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeAuditStore(
    var items: MutableMap<String, EvChargeAudit>,
    var order: MutableList<String>,
) : EvChargeAuditStore {
    override fun find(code: String): EvChargeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeAudit) {
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

    override fun list(): MutableList<EvChargeAudit> {
        val out = mutableListOf<EvChargeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeAuditStore(): EvChargeAuditStore {
    return MemoryEvChargeAuditStore(mutableMapOf<String, EvChargeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeAuditStore(
    var inner: EvChargeAuditStore,
    var cache: TtlCache<EvChargeAudit>,
    var hits: Int,
    var misses: Int,
) : EvChargeAuditStore {
    override fun find(code: String): EvChargeAudit? {
        val hit = (cache.get(code) as EvChargeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeAudit> = inner.list()
}

fun newCachedEvChargeAuditStore(inner: EvChargeAuditStore, ttl: Int, clock: Clock): EvChargeAuditStore {
    return CachedEvChargeAuditStore(
        inner = inner,
        cache = newTtlCache<EvChargeAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge audits. Empty fields match all. */
data class EvChargeAuditQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeAudit): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeAudit>): MutableList<EvChargeAudit> {
        val matched = filter(items) { candidate: EvChargeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeAudits(store: EvChargeAuditStore, query: EvChargeAuditQuery): MutableList<EvChargeAudit> {
    return query.apply(store.list())
}
