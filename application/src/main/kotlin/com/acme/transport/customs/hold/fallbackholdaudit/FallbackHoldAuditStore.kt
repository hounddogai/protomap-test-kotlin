package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback hold audits by code. */
interface FallbackHoldAuditStore {
    fun find(code: String): FallbackHoldAudit?
    fun save(item: FallbackHoldAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackHoldAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackHoldAuditStore(
    var items: MutableMap<String, FallbackHoldAudit>,
    var order: MutableList<String>,
) : FallbackHoldAuditStore {
    override fun find(code: String): FallbackHoldAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackHoldAudit) {
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

    override fun list(): MutableList<FallbackHoldAudit> {
        val out = mutableListOf<FallbackHoldAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackHoldAuditStore(): FallbackHoldAuditStore {
    return MemoryFallbackHoldAuditStore(mutableMapOf<String, FallbackHoldAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackHoldAuditStore(
    var inner: FallbackHoldAuditStore,
    var cache: TtlCache<FallbackHoldAudit>,
    var hits: Int,
    var misses: Int,
) : FallbackHoldAuditStore {
    override fun find(code: String): FallbackHoldAudit? {
        val hit = (cache.get(code) as FallbackHoldAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackHoldAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackHoldAudit> = inner.list()
}

fun newCachedFallbackHoldAuditStore(inner: FallbackHoldAuditStore, ttl: Int, clock: Clock): FallbackHoldAuditStore {
    return CachedFallbackHoldAuditStore(
        inner = inner,
        cache = newTtlCache<FallbackHoldAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback hold audits. Empty fields match all. */
data class FallbackHoldAuditQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackHoldAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackHoldAudit>): MutableList<FallbackHoldAudit> {
        val matched = filter(items) { candidate: FallbackHoldAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackHoldAudits(store: FallbackHoldAuditStore, query: FallbackHoldAuditQuery): MutableList<FallbackHoldAudit> {
    return query.apply(store.list())
}
