package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard cycles by code. */
interface ScorecardCycleStore {
    fun find(code: String): ScorecardCycle?
    fun save(item: ScorecardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardCycleStore(
    var items: MutableMap<String, ScorecardCycle>,
    var order: MutableList<String>,
) : ScorecardCycleStore {
    override fun find(code: String): ScorecardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardCycle) {
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

    override fun list(): MutableList<ScorecardCycle> {
        val out = mutableListOf<ScorecardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardCycleStore(): ScorecardCycleStore {
    return MemoryScorecardCycleStore(mutableMapOf<String, ScorecardCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardCycleStore(
    var inner: ScorecardCycleStore,
    var cache: TtlCache<ScorecardCycle>,
    var hits: Int,
    var misses: Int,
) : ScorecardCycleStore {
    override fun find(code: String): ScorecardCycle? {
        val hit = (cache.get(code) as ScorecardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardCycle> = inner.list()
}

fun newCachedScorecardCycleStore(inner: ScorecardCycleStore, ttl: Int, clock: Clock): ScorecardCycleStore {
    return CachedScorecardCycleStore(
        inner = inner,
        cache = newTtlCache<ScorecardCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard cycles. Empty fields match all. */
data class ScorecardCycleQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardCycle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardCycle>): MutableList<ScorecardCycle> {
        val matched = filter(items) { candidate: ScorecardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardCycles(store: ScorecardCycleStore, query: ScorecardCycleQuery): MutableList<ScorecardCycle> {
    return query.apply(store.list())
}
