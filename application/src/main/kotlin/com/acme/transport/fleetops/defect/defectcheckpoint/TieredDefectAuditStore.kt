package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered defect audits by code. */
interface TieredDefectAuditStore {
    fun find(code: String): TieredDefectAudit?
    fun save(item: TieredDefectAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDefectAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDefectAuditStore(
    var items: MutableMap<String, TieredDefectAudit>,
    var order: MutableList<String>,
) : TieredDefectAuditStore {
    override fun find(code: String): TieredDefectAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDefectAudit) {
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

    override fun list(): MutableList<TieredDefectAudit> {
        val out = mutableListOf<TieredDefectAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDefectAuditStore(): TieredDefectAuditStore {
    return MemoryTieredDefectAuditStore(mutableMapOf<String, TieredDefectAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDefectAuditStore(
    var inner: TieredDefectAuditStore,
    var cache: TtlCache<TieredDefectAudit>,
    var hits: Int,
    var misses: Int,
) : TieredDefectAuditStore {
    override fun find(code: String): TieredDefectAudit? {
        val hit = (cache.get(code) as TieredDefectAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDefectAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDefectAudit> = inner.list()
}

fun newCachedTieredDefectAuditStore(inner: TieredDefectAuditStore, ttl: Int, clock: Clock): TieredDefectAuditStore {
    return CachedTieredDefectAuditStore(
        inner = inner,
        cache = newTtlCache<TieredDefectAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered defect audits. Empty fields match all. */
data class TieredDefectAuditQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDefectAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDefectAudit>): MutableList<TieredDefectAudit> {
        val matched = filter(items) { candidate: TieredDefectAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDefectAudits(store: TieredDefectAuditStore, query: TieredDefectAuditQuery): MutableList<TieredDefectAudit> {
    return query.apply(store.list())
}
