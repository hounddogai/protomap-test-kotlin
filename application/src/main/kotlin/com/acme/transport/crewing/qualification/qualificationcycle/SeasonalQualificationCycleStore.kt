package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal qualification cycles by code. */
interface SeasonalQualificationCycleStore {
    fun find(code: String): SeasonalQualificationCycle?
    fun save(item: SeasonalQualificationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalQualificationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalQualificationCycleStore(
    var items: MutableMap<String, SeasonalQualificationCycle>,
    var order: MutableList<String>,
) : SeasonalQualificationCycleStore {
    override fun find(code: String): SeasonalQualificationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalQualificationCycle) {
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

    override fun list(): MutableList<SeasonalQualificationCycle> {
        val out = mutableListOf<SeasonalQualificationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalQualificationCycleStore(): SeasonalQualificationCycleStore {
    return MemorySeasonalQualificationCycleStore(
        mutableMapOf<String, SeasonalQualificationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalQualificationCycleStore(
    var inner: SeasonalQualificationCycleStore,
    var cache: TtlCache<SeasonalQualificationCycle>,
    var hits: Int,
    var misses: Int,
) : SeasonalQualificationCycleStore {
    override fun find(code: String): SeasonalQualificationCycle? {
        val hit = (cache.get(code) as SeasonalQualificationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalQualificationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalQualificationCycle> = inner.list()
}

fun newCachedSeasonalQualificationCycleStore(inner: SeasonalQualificationCycleStore, ttl: Int, clock: Clock): SeasonalQualificationCycleStore {
    return CachedSeasonalQualificationCycleStore(
        inner = inner,
        cache = newTtlCache<SeasonalQualificationCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal qualification cycles. Empty fields match all. */
data class SeasonalQualificationCycleQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalQualificationCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalQualificationCycle>): MutableList<SeasonalQualificationCycle> {
        val matched = filter(items) { candidate: SeasonalQualificationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalQualificationCycles(store: SeasonalQualificationCycleStore, query: SeasonalQualificationCycleQuery): MutableList<SeasonalQualificationCycle> {
    return query.apply(store.list())
}
