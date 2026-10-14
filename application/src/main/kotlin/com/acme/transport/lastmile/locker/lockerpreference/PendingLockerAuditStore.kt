package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending locker audits by code. */
interface PendingLockerAuditStore {
    fun find(code: String): PendingLockerAudit?
    fun save(item: PendingLockerAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLockerAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLockerAuditStore(
    var items: MutableMap<String, PendingLockerAudit>,
    var order: MutableList<String>,
) : PendingLockerAuditStore {
    override fun find(code: String): PendingLockerAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLockerAudit) {
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

    override fun list(): MutableList<PendingLockerAudit> {
        val out = mutableListOf<PendingLockerAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLockerAuditStore(): PendingLockerAuditStore {
    return MemoryPendingLockerAuditStore(mutableMapOf<String, PendingLockerAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLockerAuditStore(
    var inner: PendingLockerAuditStore,
    var cache: TtlCache<PendingLockerAudit>,
    var hits: Int,
    var misses: Int,
) : PendingLockerAuditStore {
    override fun find(code: String): PendingLockerAudit? {
        val hit = (cache.get(code) as PendingLockerAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLockerAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLockerAudit> = inner.list()
}

fun newCachedPendingLockerAuditStore(inner: PendingLockerAuditStore, ttl: Int, clock: Clock): PendingLockerAuditStore {
    return CachedPendingLockerAuditStore(
        inner = inner,
        cache = newTtlCache<PendingLockerAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending locker audits. Empty fields match all. */
data class PendingLockerAuditQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLockerAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLockerAudit>): MutableList<PendingLockerAudit> {
        val matched = filter(items) { candidate: PendingLockerAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLockerAudits(store: PendingLockerAuditStore, query: PendingLockerAuditQuery): MutableList<PendingLockerAudit> {
    return query.apply(store.list())
}
