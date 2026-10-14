package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict backhaul audits by code. */
interface StrictBackhaulAuditStore {
    fun find(code: String): StrictBackhaulAudit?
    fun save(item: StrictBackhaulAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBackhaulAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBackhaulAuditStore(
    var items: MutableMap<String, StrictBackhaulAudit>,
    var order: MutableList<String>,
) : StrictBackhaulAuditStore {
    override fun find(code: String): StrictBackhaulAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBackhaulAudit) {
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

    override fun list(): MutableList<StrictBackhaulAudit> {
        val out = mutableListOf<StrictBackhaulAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBackhaulAuditStore(): StrictBackhaulAuditStore {
    return MemoryStrictBackhaulAuditStore(mutableMapOf<String, StrictBackhaulAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBackhaulAuditStore(
    var inner: StrictBackhaulAuditStore,
    var cache: TtlCache<StrictBackhaulAudit>,
    var hits: Int,
    var misses: Int,
) : StrictBackhaulAuditStore {
    override fun find(code: String): StrictBackhaulAudit? {
        val hit = (cache.get(code) as StrictBackhaulAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBackhaulAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBackhaulAudit> = inner.list()
}

fun newCachedStrictBackhaulAuditStore(inner: StrictBackhaulAuditStore, ttl: Int, clock: Clock): StrictBackhaulAuditStore {
    return CachedStrictBackhaulAuditStore(
        inner = inner,
        cache = newTtlCache<StrictBackhaulAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict backhaul audits. Empty fields match all. */
data class StrictBackhaulAuditQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBackhaulAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBackhaulAudit>): MutableList<StrictBackhaulAudit> {
        val matched = filter(items) { candidate: StrictBackhaulAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBackhaulAudits(store: StrictBackhaulAuditStore, query: StrictBackhaulAuditQuery): MutableList<StrictBackhaulAudit> {
    return query.apply(store.list())
}
