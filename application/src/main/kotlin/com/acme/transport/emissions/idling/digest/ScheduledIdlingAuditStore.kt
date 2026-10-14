package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled idling audits by code. */
interface ScheduledIdlingAuditStore {
    fun find(code: String): ScheduledIdlingAudit?
    fun save(item: ScheduledIdlingAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledIdlingAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledIdlingAuditStore(
    var items: MutableMap<String, ScheduledIdlingAudit>,
    var order: MutableList<String>,
) : ScheduledIdlingAuditStore {
    override fun find(code: String): ScheduledIdlingAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledIdlingAudit) {
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

    override fun list(): MutableList<ScheduledIdlingAudit> {
        val out = mutableListOf<ScheduledIdlingAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledIdlingAuditStore(): ScheduledIdlingAuditStore {
    return MemoryScheduledIdlingAuditStore(
        mutableMapOf<String, ScheduledIdlingAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledIdlingAuditStore(
    var inner: ScheduledIdlingAuditStore,
    var cache: TtlCache<ScheduledIdlingAudit>,
    var hits: Int,
    var misses: Int,
) : ScheduledIdlingAuditStore {
    override fun find(code: String): ScheduledIdlingAudit? {
        val hit = (cache.get(code) as ScheduledIdlingAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledIdlingAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledIdlingAudit> = inner.list()
}

fun newCachedScheduledIdlingAuditStore(inner: ScheduledIdlingAuditStore, ttl: Int, clock: Clock): ScheduledIdlingAuditStore {
    return CachedScheduledIdlingAuditStore(
        inner = inner,
        cache = newTtlCache<ScheduledIdlingAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled idling audits. Empty fields match all. */
data class ScheduledIdlingAuditQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledIdlingAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledIdlingAudit>): MutableList<ScheduledIdlingAudit> {
        val matched = filter(items) { candidate: ScheduledIdlingAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledIdlingAudits(store: ScheduledIdlingAuditStore, query: ScheduledIdlingAuditQuery): MutableList<ScheduledIdlingAudit> {
    return query.apply(store.list())
}
