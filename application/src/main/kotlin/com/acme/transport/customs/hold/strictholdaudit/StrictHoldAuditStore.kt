package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict hold audits by code. */
interface StrictHoldAuditStore {
    fun find(code: String): StrictHoldAudit?
    fun save(item: StrictHoldAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictHoldAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictHoldAuditStore(
    var items: MutableMap<String, StrictHoldAudit>,
    var order: MutableList<String>,
) : StrictHoldAuditStore {
    override fun find(code: String): StrictHoldAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictHoldAudit) {
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

    override fun list(): MutableList<StrictHoldAudit> {
        val out = mutableListOf<StrictHoldAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictHoldAuditStore(): StrictHoldAuditStore {
    return MemoryStrictHoldAuditStore(mutableMapOf<String, StrictHoldAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictHoldAuditStore(
    var inner: StrictHoldAuditStore,
    var cache: TtlCache<StrictHoldAudit>,
    var hits: Int,
    var misses: Int,
) : StrictHoldAuditStore {
    override fun find(code: String): StrictHoldAudit? {
        val hit = (cache.get(code) as StrictHoldAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictHoldAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictHoldAudit> = inner.list()
}

fun newCachedStrictHoldAuditStore(inner: StrictHoldAuditStore, ttl: Int, clock: Clock): StrictHoldAuditStore {
    return CachedStrictHoldAuditStore(
        inner = inner,
        cache = newTtlCache<StrictHoldAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict hold audits. Empty fields match all. */
data class StrictHoldAuditQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictHoldAudit): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictHoldAudit>): MutableList<StrictHoldAudit> {
        val matched = filter(items) { candidate: StrictHoldAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictHoldAudits(store: StrictHoldAuditStore, query: StrictHoldAuditQuery): MutableList<StrictHoldAudit> {
    return query.apply(store.list())
}
