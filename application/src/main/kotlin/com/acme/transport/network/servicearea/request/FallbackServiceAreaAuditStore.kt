package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback service area audits by code. */
interface FallbackServiceAreaAuditStore {
    fun find(code: String): FallbackServiceAreaAudit?
    fun save(item: FallbackServiceAreaAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServiceAreaAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServiceAreaAuditStore(
    var items: MutableMap<String, FallbackServiceAreaAudit>,
    var order: MutableList<String>,
) : FallbackServiceAreaAuditStore {
    override fun find(code: String): FallbackServiceAreaAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServiceAreaAudit) {
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

    override fun list(): MutableList<FallbackServiceAreaAudit> {
        val out = mutableListOf<FallbackServiceAreaAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServiceAreaAuditStore(): FallbackServiceAreaAuditStore {
    return MemoryFallbackServiceAreaAuditStore(
        mutableMapOf<String, FallbackServiceAreaAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServiceAreaAuditStore(
    var inner: FallbackServiceAreaAuditStore,
    var cache: TtlCache<FallbackServiceAreaAudit>,
    var hits: Int,
    var misses: Int,
) : FallbackServiceAreaAuditStore {
    override fun find(code: String): FallbackServiceAreaAudit? {
        val hit = (cache.get(code) as FallbackServiceAreaAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServiceAreaAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServiceAreaAudit> = inner.list()
}

fun newCachedFallbackServiceAreaAuditStore(inner: FallbackServiceAreaAuditStore, ttl: Int, clock: Clock): FallbackServiceAreaAuditStore {
    return CachedFallbackServiceAreaAuditStore(
        inner = inner,
        cache = newTtlCache<FallbackServiceAreaAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service area audits. Empty fields match all. */
data class FallbackServiceAreaAuditQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServiceAreaAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackServiceAreaAudit>): MutableList<FallbackServiceAreaAudit> {
        val matched = filter(items) { candidate: FallbackServiceAreaAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServiceAreaAudits(store: FallbackServiceAreaAuditStore, query: FallbackServiceAreaAuditQuery): MutableList<FallbackServiceAreaAudit> {
    return query.apply(store.list())
}
