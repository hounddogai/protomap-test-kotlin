package com.acme.transport.network.hub.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled hub audits by code. */
interface ScheduledHubAuditStore {
    fun find(code: String): ScheduledHubAudit?
    fun save(item: ScheduledHubAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHubAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHubAuditStore(
    var items: MutableMap<String, ScheduledHubAudit>,
    var order: MutableList<String>,
) : ScheduledHubAuditStore {
    override fun find(code: String): ScheduledHubAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHubAudit) {
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

    override fun list(): MutableList<ScheduledHubAudit> {
        val out = mutableListOf<ScheduledHubAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHubAuditStore(): ScheduledHubAuditStore {
    return MemoryScheduledHubAuditStore(mutableMapOf<String, ScheduledHubAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHubAuditStore(
    var inner: ScheduledHubAuditStore,
    var cache: TtlCache<ScheduledHubAudit>,
    var hits: Int,
    var misses: Int,
) : ScheduledHubAuditStore {
    override fun find(code: String): ScheduledHubAudit? {
        val hit = (cache.get(code) as ScheduledHubAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHubAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHubAudit> = inner.list()
}

fun newCachedScheduledHubAuditStore(inner: ScheduledHubAuditStore, ttl: Int, clock: Clock): ScheduledHubAuditStore {
    return CachedScheduledHubAuditStore(
        inner = inner,
        cache = newTtlCache<ScheduledHubAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hub audits. Empty fields match all. */
data class ScheduledHubAuditQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHubAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHubAudit>): MutableList<ScheduledHubAudit> {
        val matched = filter(items) { candidate: ScheduledHubAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHubAudits(store: ScheduledHubAuditStore, query: ScheduledHubAuditQuery): MutableList<ScheduledHubAudit> {
    return query.apply(store.list())
}
