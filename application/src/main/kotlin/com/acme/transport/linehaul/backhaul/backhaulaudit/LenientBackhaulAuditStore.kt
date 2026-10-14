package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient backhaul audits by code. */
interface LenientBackhaulAuditStore {
    fun find(code: String): LenientBackhaulAudit?
    fun save(item: LenientBackhaulAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBackhaulAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBackhaulAuditStore(
    var items: MutableMap<String, LenientBackhaulAudit>,
    var order: MutableList<String>,
) : LenientBackhaulAuditStore {
    override fun find(code: String): LenientBackhaulAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBackhaulAudit) {
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

    override fun list(): MutableList<LenientBackhaulAudit> {
        val out = mutableListOf<LenientBackhaulAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBackhaulAuditStore(): LenientBackhaulAuditStore {
    return MemoryLenientBackhaulAuditStore(
        mutableMapOf<String, LenientBackhaulAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBackhaulAuditStore(
    var inner: LenientBackhaulAuditStore,
    var cache: TtlCache<LenientBackhaulAudit>,
    var hits: Int,
    var misses: Int,
) : LenientBackhaulAuditStore {
    override fun find(code: String): LenientBackhaulAudit? {
        val hit = (cache.get(code) as LenientBackhaulAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBackhaulAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBackhaulAudit> = inner.list()
}

fun newCachedLenientBackhaulAuditStore(inner: LenientBackhaulAuditStore, ttl: Int, clock: Clock): LenientBackhaulAuditStore {
    return CachedLenientBackhaulAuditStore(
        inner = inner,
        cache = newTtlCache<LenientBackhaulAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient backhaul audits. Empty fields match all. */
data class LenientBackhaulAuditQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBackhaulAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientBackhaulAudit>): MutableList<LenientBackhaulAudit> {
        val matched = filter(items) { candidate: LenientBackhaulAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBackhaulAudits(store: LenientBackhaulAuditStore, query: LenientBackhaulAuditQuery): MutableList<LenientBackhaulAudit> {
    return query.apply(store.list())
}
