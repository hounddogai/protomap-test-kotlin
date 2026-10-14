package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global duty period rollups by code. */
interface GlobalDutyPeriodRollupStore {
    fun find(code: String): GlobalDutyPeriodRollup?
    fun save(item: GlobalDutyPeriodRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDutyPeriodRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDutyPeriodRollupStore(
    var items: MutableMap<String, GlobalDutyPeriodRollup>,
    var order: MutableList<String>,
) : GlobalDutyPeriodRollupStore {
    override fun find(code: String): GlobalDutyPeriodRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDutyPeriodRollup) {
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

    override fun list(): MutableList<GlobalDutyPeriodRollup> {
        val out = mutableListOf<GlobalDutyPeriodRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDutyPeriodRollupStore(): GlobalDutyPeriodRollupStore {
    return MemoryGlobalDutyPeriodRollupStore(
        mutableMapOf<String, GlobalDutyPeriodRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDutyPeriodRollupStore(
    var inner: GlobalDutyPeriodRollupStore,
    var cache: TtlCache<GlobalDutyPeriodRollup>,
    var hits: Int,
    var misses: Int,
) : GlobalDutyPeriodRollupStore {
    override fun find(code: String): GlobalDutyPeriodRollup? {
        val hit = (cache.get(code) as GlobalDutyPeriodRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDutyPeriodRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDutyPeriodRollup> = inner.list()
}

fun newCachedGlobalDutyPeriodRollupStore(inner: GlobalDutyPeriodRollupStore, ttl: Int, clock: Clock): GlobalDutyPeriodRollupStore {
    return CachedGlobalDutyPeriodRollupStore(
        inner = inner,
        cache = newTtlCache<GlobalDutyPeriodRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global duty period rollups. Empty fields match all. */
data class GlobalDutyPeriodRollupQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDutyPeriodRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDutyPeriodRollup>): MutableList<GlobalDutyPeriodRollup> {
        val matched = filter(items) { candidate: GlobalDutyPeriodRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDutyPeriodRollups(store: GlobalDutyPeriodRollupStore, query: GlobalDutyPeriodRollupQuery): MutableList<GlobalDutyPeriodRollup> {
    return query.apply(store.list())
}
