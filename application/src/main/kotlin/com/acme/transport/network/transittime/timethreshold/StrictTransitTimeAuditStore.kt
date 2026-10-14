package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict transit time audits by code. */
interface StrictTransitTimeAuditStore {
    fun find(code: String): StrictTransitTimeAudit?
    fun save(item: StrictTransitTimeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTransitTimeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTransitTimeAuditStore(
    var items: MutableMap<String, StrictTransitTimeAudit>,
    var order: MutableList<String>,
) : StrictTransitTimeAuditStore {
    override fun find(code: String): StrictTransitTimeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTransitTimeAudit) {
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

    override fun list(): MutableList<StrictTransitTimeAudit> {
        val out = mutableListOf<StrictTransitTimeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTransitTimeAuditStore(): StrictTransitTimeAuditStore {
    return MemoryStrictTransitTimeAuditStore(
        mutableMapOf<String, StrictTransitTimeAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTransitTimeAuditStore(
    var inner: StrictTransitTimeAuditStore,
    var cache: TtlCache<StrictTransitTimeAudit>,
    var hits: Int,
    var misses: Int,
) : StrictTransitTimeAuditStore {
    override fun find(code: String): StrictTransitTimeAudit? {
        val hit = (cache.get(code) as StrictTransitTimeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTransitTimeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTransitTimeAudit> = inner.list()
}

fun newCachedStrictTransitTimeAuditStore(inner: StrictTransitTimeAuditStore, ttl: Int, clock: Clock): StrictTransitTimeAuditStore {
    return CachedStrictTransitTimeAuditStore(
        inner = inner,
        cache = newTtlCache<StrictTransitTimeAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict transit time audits. Empty fields match all. */
data class StrictTransitTimeAuditQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTransitTimeAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTransitTimeAudit>): MutableList<StrictTransitTimeAudit> {
        val matched = filter(items) { candidate: StrictTransitTimeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTransitTimeAudits(store: StrictTransitTimeAuditStore, query: StrictTransitTimeAuditQuery): MutableList<StrictTransitTimeAudit> {
    return query.apply(store.list())
}
