package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan audits by code. */
interface LanePlanAuditStore {
    fun find(code: String): LanePlanAudit?
    fun save(item: LanePlanAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanAuditStore(
    var items: MutableMap<String, LanePlanAudit>,
    var order: MutableList<String>,
) : LanePlanAuditStore {
    override fun find(code: String): LanePlanAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanAudit) {
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

    override fun list(): MutableList<LanePlanAudit> {
        val out = mutableListOf<LanePlanAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanAuditStore(): LanePlanAuditStore {
    return MemoryLanePlanAuditStore(mutableMapOf<String, LanePlanAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanAuditStore(
    var inner: LanePlanAuditStore,
    var cache: TtlCache<LanePlanAudit>,
    var hits: Int,
    var misses: Int,
) : LanePlanAuditStore {
    override fun find(code: String): LanePlanAudit? {
        val hit = (cache.get(code) as LanePlanAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanAudit> = inner.list()
}

fun newCachedLanePlanAuditStore(inner: LanePlanAuditStore, ttl: Int, clock: Clock): LanePlanAuditStore {
    return CachedLanePlanAuditStore(
        inner = inner,
        cache = newTtlCache<LanePlanAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan audits. Empty fields match all. */
data class LanePlanAuditQuery(
    var region: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanAudit): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanAudit>): MutableList<LanePlanAudit> {
        val matched = filter(items) { candidate: LanePlanAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanAudits(store: LanePlanAuditStore, query: LanePlanAuditQuery): MutableList<LanePlanAudit> {
    return query.apply(store.list())
}
