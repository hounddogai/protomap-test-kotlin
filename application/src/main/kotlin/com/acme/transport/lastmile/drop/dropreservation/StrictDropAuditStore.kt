package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict drop audits by code. */
interface StrictDropAuditStore {
    fun find(code: String): StrictDropAudit?
    fun save(item: StrictDropAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDropAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDropAuditStore(
    var items: MutableMap<String, StrictDropAudit>,
    var order: MutableList<String>,
) : StrictDropAuditStore {
    override fun find(code: String): StrictDropAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDropAudit) {
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

    override fun list(): MutableList<StrictDropAudit> {
        val out = mutableListOf<StrictDropAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDropAuditStore(): StrictDropAuditStore {
    return MemoryStrictDropAuditStore(mutableMapOf<String, StrictDropAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDropAuditStore(
    var inner: StrictDropAuditStore,
    var cache: TtlCache<StrictDropAudit>,
    var hits: Int,
    var misses: Int,
) : StrictDropAuditStore {
    override fun find(code: String): StrictDropAudit? {
        val hit = (cache.get(code) as StrictDropAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDropAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDropAudit> = inner.list()
}

fun newCachedStrictDropAuditStore(inner: StrictDropAuditStore, ttl: Int, clock: Clock): StrictDropAuditStore {
    return CachedStrictDropAuditStore(
        inner = inner,
        cache = newTtlCache<StrictDropAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict drop audits. Empty fields match all. */
data class StrictDropAuditQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDropAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDropAudit>): MutableList<StrictDropAudit> {
        val matched = filter(items) { candidate: StrictDropAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDropAudits(store: StrictDropAuditStore, query: StrictDropAuditQuery): MutableList<StrictDropAudit> {
    return query.apply(store.list())
}
