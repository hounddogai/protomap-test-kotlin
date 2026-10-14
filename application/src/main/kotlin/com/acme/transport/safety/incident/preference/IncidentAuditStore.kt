package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident audits by code. */
interface IncidentAuditStore {
    fun find(code: String): IncidentAudit?
    fun save(item: IncidentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentAuditStore(
    var items: MutableMap<String, IncidentAudit>,
    var order: MutableList<String>,
) : IncidentAuditStore {
    override fun find(code: String): IncidentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentAudit) {
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

    override fun list(): MutableList<IncidentAudit> {
        val out = mutableListOf<IncidentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentAuditStore(): IncidentAuditStore {
    return MemoryIncidentAuditStore(mutableMapOf<String, IncidentAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentAuditStore(
    var inner: IncidentAuditStore,
    var cache: TtlCache<IncidentAudit>,
    var hits: Int,
    var misses: Int,
) : IncidentAuditStore {
    override fun find(code: String): IncidentAudit? {
        val hit = (cache.get(code) as IncidentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentAudit> = inner.list()
}

fun newCachedIncidentAuditStore(inner: IncidentAuditStore, ttl: Int, clock: Clock): IncidentAuditStore {
    return CachedIncidentAuditStore(
        inner = inner,
        cache = newTtlCache<IncidentAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident audits. Empty fields match all. */
data class IncidentAuditQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentAudit>): MutableList<IncidentAudit> {
        val matched = filter(items) { candidate: IncidentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentAudits(store: IncidentAuditStore, query: IncidentAuditQuery): MutableList<IncidentAudit> {
    return query.apply(store.list())
}
