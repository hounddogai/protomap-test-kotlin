package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient audit estimates by code. */
interface LenientAuditEstimateStore {
    fun find(code: String): LenientAuditEstimate?
    fun save(item: LenientAuditEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAuditEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAuditEstimateStore(
    var items: MutableMap<String, LenientAuditEstimate>,
    var order: MutableList<String>,
) : LenientAuditEstimateStore {
    override fun find(code: String): LenientAuditEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAuditEstimate) {
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

    override fun list(): MutableList<LenientAuditEstimate> {
        val out = mutableListOf<LenientAuditEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAuditEstimateStore(): LenientAuditEstimateStore {
    return MemoryLenientAuditEstimateStore(
        mutableMapOf<String, LenientAuditEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAuditEstimateStore(
    var inner: LenientAuditEstimateStore,
    var cache: TtlCache<LenientAuditEstimate>,
    var hits: Int,
    var misses: Int,
) : LenientAuditEstimateStore {
    override fun find(code: String): LenientAuditEstimate? {
        val hit = (cache.get(code) as LenientAuditEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAuditEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAuditEstimate> = inner.list()
}

fun newCachedLenientAuditEstimateStore(inner: LenientAuditEstimateStore, ttl: Int, clock: Clock): LenientAuditEstimateStore {
    return CachedLenientAuditEstimateStore(
        inner = inner,
        cache = newTtlCache<LenientAuditEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient audit estimates. Empty fields match all. */
data class LenientAuditEstimateQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAuditEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAuditEstimate>): MutableList<LenientAuditEstimate> {
        val matched = filter(items) { candidate: LenientAuditEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAuditEstimates(store: LenientAuditEstimateStore, query: LenientAuditEstimateQuery): MutableList<LenientAuditEstimate> {
    return query.apply(store.list())
}
