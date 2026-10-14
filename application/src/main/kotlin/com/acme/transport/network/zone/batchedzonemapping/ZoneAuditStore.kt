package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone audits by code. */
interface ZoneAuditStore {
    fun find(code: String): ZoneAudit?
    fun save(item: ZoneAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneAuditStore(
    var items: MutableMap<String, ZoneAudit>,
    var order: MutableList<String>,
) : ZoneAuditStore {
    override fun find(code: String): ZoneAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneAudit) {
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

    override fun list(): MutableList<ZoneAudit> {
        val out = mutableListOf<ZoneAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneAuditStore(): ZoneAuditStore {
    return MemoryZoneAuditStore(mutableMapOf<String, ZoneAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneAuditStore(
    var inner: ZoneAuditStore,
    var cache: TtlCache<ZoneAudit>,
    var hits: Int,
    var misses: Int,
) : ZoneAuditStore {
    override fun find(code: String): ZoneAudit? {
        val hit = (cache.get(code) as ZoneAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneAudit> = inner.list()
}

fun newCachedZoneAuditStore(inner: ZoneAuditStore, ttl: Int, clock: Clock): ZoneAuditStore {
    return CachedZoneAuditStore(
        inner = inner,
        cache = newTtlCache<ZoneAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone audits. Empty fields match all. */
data class ZoneAuditQuery(
    var locale: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneAudit): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneAudit>): MutableList<ZoneAudit> {
        val matched = filter(items) { candidate: ZoneAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneAudits(store: ZoneAuditStore, query: ZoneAuditQuery): MutableList<ZoneAudit> {
    return query.apply(store.list())
}
