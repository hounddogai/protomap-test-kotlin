package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual drop audits by code. */
interface ManualDropAuditStore {
    fun find(code: String): ManualDropAudit?
    fun save(item: ManualDropAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDropAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDropAuditStore(
    var items: MutableMap<String, ManualDropAudit>,
    var order: MutableList<String>,
) : ManualDropAuditStore {
    override fun find(code: String): ManualDropAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDropAudit) {
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

    override fun list(): MutableList<ManualDropAudit> {
        val out = mutableListOf<ManualDropAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDropAuditStore(): ManualDropAuditStore {
    return MemoryManualDropAuditStore(mutableMapOf<String, ManualDropAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDropAuditStore(
    var inner: ManualDropAuditStore,
    var cache: TtlCache<ManualDropAudit>,
    var hits: Int,
    var misses: Int,
) : ManualDropAuditStore {
    override fun find(code: String): ManualDropAudit? {
        val hit = (cache.get(code) as ManualDropAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDropAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDropAudit> = inner.list()
}

fun newCachedManualDropAuditStore(inner: ManualDropAuditStore, ttl: Int, clock: Clock): ManualDropAuditStore {
    return CachedManualDropAuditStore(
        inner = inner,
        cache = newTtlCache<ManualDropAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual drop audits. Empty fields match all. */
data class ManualDropAuditQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDropAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ManualDropAudit>): MutableList<ManualDropAudit> {
        val matched = filter(items) { candidate: ManualDropAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDropAudits(store: ManualDropAuditStore, query: ManualDropAuditQuery): MutableList<ManualDropAudit> {
    return query.apply(store.list())
}
