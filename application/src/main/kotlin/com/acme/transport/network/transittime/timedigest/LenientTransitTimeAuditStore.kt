package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient transit time audits by code. */
interface LenientTransitTimeAuditStore {
    fun find(code: String): LenientTransitTimeAudit?
    fun save(item: LenientTransitTimeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTransitTimeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTransitTimeAuditStore(
    var items: MutableMap<String, LenientTransitTimeAudit>,
    var order: MutableList<String>,
) : LenientTransitTimeAuditStore {
    override fun find(code: String): LenientTransitTimeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTransitTimeAudit) {
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

    override fun list(): MutableList<LenientTransitTimeAudit> {
        val out = mutableListOf<LenientTransitTimeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTransitTimeAuditStore(): LenientTransitTimeAuditStore {
    return MemoryLenientTransitTimeAuditStore(
        mutableMapOf<String, LenientTransitTimeAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTransitTimeAuditStore(
    var inner: LenientTransitTimeAuditStore,
    var cache: TtlCache<LenientTransitTimeAudit>,
    var hits: Int,
    var misses: Int,
) : LenientTransitTimeAuditStore {
    override fun find(code: String): LenientTransitTimeAudit? {
        val hit = (cache.get(code) as LenientTransitTimeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTransitTimeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTransitTimeAudit> = inner.list()
}

fun newCachedLenientTransitTimeAuditStore(inner: LenientTransitTimeAuditStore, ttl: Int, clock: Clock): LenientTransitTimeAuditStore {
    return CachedLenientTransitTimeAuditStore(
        inner = inner,
        cache = newTtlCache<LenientTransitTimeAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient transit time audits. Empty fields match all. */
data class LenientTransitTimeAuditQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTransitTimeAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTransitTimeAudit>): MutableList<LenientTransitTimeAudit> {
        val matched = filter(items) { candidate: LenientTransitTimeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTransitTimeAudits(store: LenientTransitTimeAuditStore, query: LenientTransitTimeAuditQuery): MutableList<LenientTransitTimeAudit> {
    return query.apply(store.list())
}
