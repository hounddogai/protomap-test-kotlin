package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit adjustments by code. */
interface AuditAdjustmentStore {
    fun find(code: String): AuditAdjustment?
    fun save(item: AuditAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditAdjustmentStore(
    var items: MutableMap<String, AuditAdjustment>,
    var order: MutableList<String>,
) : AuditAdjustmentStore {
    override fun find(code: String): AuditAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditAdjustment) {
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

    override fun list(): MutableList<AuditAdjustment> {
        val out = mutableListOf<AuditAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditAdjustmentStore(): AuditAdjustmentStore {
    return MemoryAuditAdjustmentStore(mutableMapOf<String, AuditAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditAdjustmentStore(
    var inner: AuditAdjustmentStore,
    var cache: TtlCache<AuditAdjustment>,
    var hits: Int,
    var misses: Int,
) : AuditAdjustmentStore {
    override fun find(code: String): AuditAdjustment? {
        val hit = (cache.get(code) as AuditAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditAdjustment> = inner.list()
}

fun newCachedAuditAdjustmentStore(inner: AuditAdjustmentStore, ttl: Int, clock: Clock): AuditAdjustmentStore {
    return CachedAuditAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AuditAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit adjustments. Empty fields match all. */
data class AuditAdjustmentQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<AuditAdjustment>): MutableList<AuditAdjustment> {
        val matched = filter(items) { candidate: AuditAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditAdjustments(store: AuditAdjustmentStore, query: AuditAdjustmentQuery): MutableList<AuditAdjustment> {
    return query.apply(store.list())
}
