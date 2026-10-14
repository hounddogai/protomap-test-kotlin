package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient redelivery audits by code. */
interface LenientRedeliveryAuditStore {
    fun find(code: String): LenientRedeliveryAudit?
    fun save(item: LenientRedeliveryAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRedeliveryAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRedeliveryAuditStore(
    var items: MutableMap<String, LenientRedeliveryAudit>,
    var order: MutableList<String>,
) : LenientRedeliveryAuditStore {
    override fun find(code: String): LenientRedeliveryAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRedeliveryAudit) {
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

    override fun list(): MutableList<LenientRedeliveryAudit> {
        val out = mutableListOf<LenientRedeliveryAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRedeliveryAuditStore(): LenientRedeliveryAuditStore {
    return MemoryLenientRedeliveryAuditStore(
        mutableMapOf<String, LenientRedeliveryAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRedeliveryAuditStore(
    var inner: LenientRedeliveryAuditStore,
    var cache: TtlCache<LenientRedeliveryAudit>,
    var hits: Int,
    var misses: Int,
) : LenientRedeliveryAuditStore {
    override fun find(code: String): LenientRedeliveryAudit? {
        val hit = (cache.get(code) as LenientRedeliveryAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRedeliveryAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRedeliveryAudit> = inner.list()
}

fun newCachedLenientRedeliveryAuditStore(inner: LenientRedeliveryAuditStore, ttl: Int, clock: Clock): LenientRedeliveryAuditStore {
    return CachedLenientRedeliveryAuditStore(
        inner = inner,
        cache = newTtlCache<LenientRedeliveryAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient redelivery audits. Empty fields match all. */
data class LenientRedeliveryAuditQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRedeliveryAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRedeliveryAudit>): MutableList<LenientRedeliveryAudit> {
        val matched = filter(items) { candidate: LenientRedeliveryAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRedeliveryAudits(store: LenientRedeliveryAuditStore, query: LenientRedeliveryAuditQuery): MutableList<LenientRedeliveryAudit> {
    return query.apply(store.list())
}
