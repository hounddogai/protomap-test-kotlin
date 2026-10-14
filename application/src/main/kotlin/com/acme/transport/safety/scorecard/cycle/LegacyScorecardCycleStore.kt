package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy scorecard cycles by code. */
interface LegacyScorecardCycleStore {
    fun find(code: String): LegacyScorecardCycle?
    fun save(item: LegacyScorecardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyScorecardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyScorecardCycleStore(
    var items: MutableMap<String, LegacyScorecardCycle>,
    var order: MutableList<String>,
) : LegacyScorecardCycleStore {
    override fun find(code: String): LegacyScorecardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyScorecardCycle) {
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

    override fun list(): MutableList<LegacyScorecardCycle> {
        val out = mutableListOf<LegacyScorecardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyScorecardCycleStore(): LegacyScorecardCycleStore {
    return MemoryLegacyScorecardCycleStore(
        mutableMapOf<String, LegacyScorecardCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyScorecardCycleStore(
    var inner: LegacyScorecardCycleStore,
    var cache: TtlCache<LegacyScorecardCycle>,
    var hits: Int,
    var misses: Int,
) : LegacyScorecardCycleStore {
    override fun find(code: String): LegacyScorecardCycle? {
        val hit = (cache.get(code) as LegacyScorecardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyScorecardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyScorecardCycle> = inner.list()
}

fun newCachedLegacyScorecardCycleStore(inner: LegacyScorecardCycleStore, ttl: Int, clock: Clock): LegacyScorecardCycleStore {
    return CachedLegacyScorecardCycleStore(
        inner = inner,
        cache = newTtlCache<LegacyScorecardCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy scorecard cycles. Empty fields match all. */
data class LegacyScorecardCycleQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyScorecardCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyScorecardCycle>): MutableList<LegacyScorecardCycle> {
        val matched = filter(items) { candidate: LegacyScorecardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyScorecardCycles(store: LegacyScorecardCycleStore, query: LegacyScorecardCycleQuery): MutableList<LegacyScorecardCycle> {
    return query.apply(store.list())
}
