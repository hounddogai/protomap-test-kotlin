package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered scorecard cycles by code. */
interface TieredScorecardCycleStore {
    fun find(code: String): TieredScorecardCycle?
    fun save(item: TieredScorecardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredScorecardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredScorecardCycleStore(
    var items: MutableMap<String, TieredScorecardCycle>,
    var order: MutableList<String>,
) : TieredScorecardCycleStore {
    override fun find(code: String): TieredScorecardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredScorecardCycle) {
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

    override fun list(): MutableList<TieredScorecardCycle> {
        val out = mutableListOf<TieredScorecardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredScorecardCycleStore(): TieredScorecardCycleStore {
    return MemoryTieredScorecardCycleStore(
        mutableMapOf<String, TieredScorecardCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredScorecardCycleStore(
    var inner: TieredScorecardCycleStore,
    var cache: TtlCache<TieredScorecardCycle>,
    var hits: Int,
    var misses: Int,
) : TieredScorecardCycleStore {
    override fun find(code: String): TieredScorecardCycle? {
        val hit = (cache.get(code) as TieredScorecardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredScorecardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredScorecardCycle> = inner.list()
}

fun newCachedTieredScorecardCycleStore(inner: TieredScorecardCycleStore, ttl: Int, clock: Clock): TieredScorecardCycleStore {
    return CachedTieredScorecardCycleStore(
        inner = inner,
        cache = newTtlCache<TieredScorecardCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered scorecard cycles. Empty fields match all. */
data class TieredScorecardCycleQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredScorecardCycle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredScorecardCycle>): MutableList<TieredScorecardCycle> {
        val matched = filter(items) { candidate: TieredScorecardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredScorecardCycles(store: TieredScorecardCycleStore, query: TieredScorecardCycleQuery): MutableList<TieredScorecardCycle> {
    return query.apply(store.list())
}
