package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation audits by code. */
interface ViolationAuditStore {
    fun find(code: String): ViolationAudit?
    fun save(item: ViolationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationAuditStore(
    var items: MutableMap<String, ViolationAudit>,
    var order: MutableList<String>,
) : ViolationAuditStore {
    override fun find(code: String): ViolationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationAudit) {
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

    override fun list(): MutableList<ViolationAudit> {
        val out = mutableListOf<ViolationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationAuditStore(): ViolationAuditStore {
    return MemoryViolationAuditStore(mutableMapOf<String, ViolationAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationAuditStore(
    var inner: ViolationAuditStore,
    var cache: TtlCache<ViolationAudit>,
    var hits: Int,
    var misses: Int,
) : ViolationAuditStore {
    override fun find(code: String): ViolationAudit? {
        val hit = (cache.get(code) as ViolationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationAudit> = inner.list()
}

fun newCachedViolationAuditStore(inner: ViolationAuditStore, ttl: Int, clock: Clock): ViolationAuditStore {
    return CachedViolationAuditStore(
        inner = inner,
        cache = newTtlCache<ViolationAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation audits. Empty fields match all. */
data class ViolationAuditQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationAudit>): MutableList<ViolationAudit> {
        val matched = filter(items) { candidate: ViolationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationAudits(store: ViolationAuditStore, query: ViolationAuditQuery): MutableList<ViolationAudit> {
    return query.apply(store.list())
}
