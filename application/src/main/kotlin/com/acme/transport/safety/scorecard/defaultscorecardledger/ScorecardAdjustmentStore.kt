package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard adjustments by code. */
interface ScorecardAdjustmentStore {
    fun find(code: String): ScorecardAdjustment?
    fun save(item: ScorecardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardAdjustmentStore(
    var items: MutableMap<String, ScorecardAdjustment>,
    var order: MutableList<String>,
) : ScorecardAdjustmentStore {
    override fun find(code: String): ScorecardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardAdjustment) {
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

    override fun list(): MutableList<ScorecardAdjustment> {
        val out = mutableListOf<ScorecardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardAdjustmentStore(): ScorecardAdjustmentStore {
    return MemoryScorecardAdjustmentStore(mutableMapOf<String, ScorecardAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardAdjustmentStore(
    var inner: ScorecardAdjustmentStore,
    var cache: TtlCache<ScorecardAdjustment>,
    var hits: Int,
    var misses: Int,
) : ScorecardAdjustmentStore {
    override fun find(code: String): ScorecardAdjustment? {
        val hit = (cache.get(code) as ScorecardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardAdjustment> = inner.list()
}

fun newCachedScorecardAdjustmentStore(inner: ScorecardAdjustmentStore, ttl: Int, clock: Clock): ScorecardAdjustmentStore {
    return CachedScorecardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ScorecardAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard adjustments. Empty fields match all. */
data class ScorecardAdjustmentQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardAdjustment>): MutableList<ScorecardAdjustment> {
        val matched = filter(items) { candidate: ScorecardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardAdjustments(store: ScorecardAdjustmentStore, query: ScorecardAdjustmentQuery): MutableList<ScorecardAdjustment> {
    return query.apply(store.list())
}
