package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive violation audits by code. */
interface AdaptiveViolationAuditStore {
    fun find(code: String): AdaptiveViolationAudit?
    fun save(item: AdaptiveViolationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveViolationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveViolationAuditStore(
    var items: MutableMap<String, AdaptiveViolationAudit>,
    var order: MutableList<String>,
) : AdaptiveViolationAuditStore {
    override fun find(code: String): AdaptiveViolationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveViolationAudit) {
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

    override fun list(): MutableList<AdaptiveViolationAudit> {
        val out = mutableListOf<AdaptiveViolationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveViolationAuditStore(): AdaptiveViolationAuditStore {
    return MemoryAdaptiveViolationAuditStore(
        mutableMapOf<String, AdaptiveViolationAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveViolationAuditStore(
    var inner: AdaptiveViolationAuditStore,
    var cache: TtlCache<AdaptiveViolationAudit>,
    var hits: Int,
    var misses: Int,
) : AdaptiveViolationAuditStore {
    override fun find(code: String): AdaptiveViolationAudit? {
        val hit = (cache.get(code) as AdaptiveViolationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveViolationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveViolationAudit> = inner.list()
}

fun newCachedAdaptiveViolationAuditStore(inner: AdaptiveViolationAuditStore, ttl: Int, clock: Clock): AdaptiveViolationAuditStore {
    return CachedAdaptiveViolationAuditStore(
        inner = inner,
        cache = newTtlCache<AdaptiveViolationAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive violation audits. Empty fields match all. */
data class AdaptiveViolationAuditQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveViolationAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveViolationAudit>): MutableList<AdaptiveViolationAudit> {
        val matched = filter(items) { candidate: AdaptiveViolationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveViolationAudits(store: AdaptiveViolationAuditStore, query: AdaptiveViolationAuditQuery): MutableList<AdaptiveViolationAudit> {
    return query.apply(store.list())
}
