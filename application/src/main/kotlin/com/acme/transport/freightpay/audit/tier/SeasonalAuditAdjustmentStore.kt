package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal audit adjustments by code. */
interface SeasonalAuditAdjustmentStore {
    fun find(code: String): SeasonalAuditAdjustment?
    fun save(item: SeasonalAuditAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAuditAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAuditAdjustmentStore(
    var items: MutableMap<String, SeasonalAuditAdjustment>,
    var order: MutableList<String>,
) : SeasonalAuditAdjustmentStore {
    override fun find(code: String): SeasonalAuditAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAuditAdjustment) {
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

    override fun list(): MutableList<SeasonalAuditAdjustment> {
        val out = mutableListOf<SeasonalAuditAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAuditAdjustmentStore(): SeasonalAuditAdjustmentStore {
    return MemorySeasonalAuditAdjustmentStore(
        mutableMapOf<String, SeasonalAuditAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAuditAdjustmentStore(
    var inner: SeasonalAuditAdjustmentStore,
    var cache: TtlCache<SeasonalAuditAdjustment>,
    var hits: Int,
    var misses: Int,
) : SeasonalAuditAdjustmentStore {
    override fun find(code: String): SeasonalAuditAdjustment? {
        val hit = (cache.get(code) as SeasonalAuditAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAuditAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAuditAdjustment> = inner.list()
}

fun newCachedSeasonalAuditAdjustmentStore(inner: SeasonalAuditAdjustmentStore, ttl: Int, clock: Clock): SeasonalAuditAdjustmentStore {
    return CachedSeasonalAuditAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalAuditAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal audit adjustments. Empty fields match all. */
data class SeasonalAuditAdjustmentQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAuditAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAuditAdjustment>): MutableList<SeasonalAuditAdjustment> {
        val matched = filter(items) { candidate: SeasonalAuditAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAuditAdjustments(store: SeasonalAuditAdjustmentStore, query: SeasonalAuditAdjustmentQuery): MutableList<SeasonalAuditAdjustment> {
    return query.apply(store.list())
}
