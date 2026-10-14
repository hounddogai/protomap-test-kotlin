package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard adjustments by code. */
interface PrimaryScorecardAdjustmentStore {
    fun find(code: String): PrimaryScorecardAdjustment?
    fun save(item: PrimaryScorecardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardAdjustmentStore(
    var items: MutableMap<String, PrimaryScorecardAdjustment>,
    var order: MutableList<String>,
) : PrimaryScorecardAdjustmentStore {
    override fun find(code: String): PrimaryScorecardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardAdjustment) {
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

    override fun list(): MutableList<PrimaryScorecardAdjustment> {
        val out = mutableListOf<PrimaryScorecardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardAdjustmentStore(): PrimaryScorecardAdjustmentStore {
    return MemoryPrimaryScorecardAdjustmentStore(
        mutableMapOf<String, PrimaryScorecardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardAdjustmentStore(
    var inner: PrimaryScorecardAdjustmentStore,
    var cache: TtlCache<PrimaryScorecardAdjustment>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardAdjustmentStore {
    override fun find(code: String): PrimaryScorecardAdjustment? {
        val hit = (cache.get(code) as PrimaryScorecardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardAdjustment> = inner.list()
}

fun newCachedPrimaryScorecardAdjustmentStore(inner: PrimaryScorecardAdjustmentStore, ttl: Int, clock: Clock): PrimaryScorecardAdjustmentStore {
    return CachedPrimaryScorecardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard adjustments. Empty fields match all. */
data class PrimaryScorecardAdjustmentQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardAdjustment>): MutableList<PrimaryScorecardAdjustment> {
        val matched = filter(items) { candidate: PrimaryScorecardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardAdjustments(store: PrimaryScorecardAdjustmentStore, query: PrimaryScorecardAdjustmentQuery): MutableList<PrimaryScorecardAdjustment> {
    return query.apply(store.list())
}
