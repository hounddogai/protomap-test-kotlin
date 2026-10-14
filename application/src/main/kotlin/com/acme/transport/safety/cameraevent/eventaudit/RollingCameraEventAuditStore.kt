package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling camera event audits by code. */
interface RollingCameraEventAuditStore {
    fun find(code: String): RollingCameraEventAudit?
    fun save(item: RollingCameraEventAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCameraEventAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCameraEventAuditStore(
    var items: MutableMap<String, RollingCameraEventAudit>,
    var order: MutableList<String>,
) : RollingCameraEventAuditStore {
    override fun find(code: String): RollingCameraEventAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCameraEventAudit) {
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

    override fun list(): MutableList<RollingCameraEventAudit> {
        val out = mutableListOf<RollingCameraEventAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCameraEventAuditStore(): RollingCameraEventAuditStore {
    return MemoryRollingCameraEventAuditStore(
        mutableMapOf<String, RollingCameraEventAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCameraEventAuditStore(
    var inner: RollingCameraEventAuditStore,
    var cache: TtlCache<RollingCameraEventAudit>,
    var hits: Int,
    var misses: Int,
) : RollingCameraEventAuditStore {
    override fun find(code: String): RollingCameraEventAudit? {
        val hit = (cache.get(code) as RollingCameraEventAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCameraEventAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCameraEventAudit> = inner.list()
}

fun newCachedRollingCameraEventAuditStore(inner: RollingCameraEventAuditStore, ttl: Int, clock: Clock): RollingCameraEventAuditStore {
    return CachedRollingCameraEventAuditStore(
        inner = inner,
        cache = newTtlCache<RollingCameraEventAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling camera event audits. Empty fields match all. */
data class RollingCameraEventAuditQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCameraEventAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCameraEventAudit>): MutableList<RollingCameraEventAudit> {
        val matched = filter(items) { candidate: RollingCameraEventAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCameraEventAudits(store: RollingCameraEventAuditStore, query: RollingCameraEventAuditQuery): MutableList<RollingCameraEventAudit> {
    return query.apply(store.list())
}
