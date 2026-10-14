package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit estimates by code. */
interface AuditEstimateStore {
    fun find(code: String): AuditEstimate?
    fun save(item: AuditEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditEstimateStore(
    var items: MutableMap<String, AuditEstimate>,
    var order: MutableList<String>,
) : AuditEstimateStore {
    override fun find(code: String): AuditEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditEstimate) {
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

    override fun list(): MutableList<AuditEstimate> {
        val out = mutableListOf<AuditEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditEstimateStore(): AuditEstimateStore {
    return MemoryAuditEstimateStore(mutableMapOf<String, AuditEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditEstimateStore(
    var inner: AuditEstimateStore,
    var cache: TtlCache<AuditEstimate>,
    var hits: Int,
    var misses: Int,
) : AuditEstimateStore {
    override fun find(code: String): AuditEstimate? {
        val hit = (cache.get(code) as AuditEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditEstimate> = inner.list()
}

fun newCachedAuditEstimateStore(inner: AuditEstimateStore, ttl: Int, clock: Clock): AuditEstimateStore {
    return CachedAuditEstimateStore(
        inner = inner,
        cache = newTtlCache<AuditEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit estimates. Empty fields match all. */
data class AuditEstimateQuery(
    var lane: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditEstimate>): MutableList<AuditEstimate> {
        val matched = filter(items) { candidate: AuditEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditEstimates(store: AuditEstimateStore, query: AuditEstimateQuery): MutableList<AuditEstimate> {
    return query.apply(store.list())
}
