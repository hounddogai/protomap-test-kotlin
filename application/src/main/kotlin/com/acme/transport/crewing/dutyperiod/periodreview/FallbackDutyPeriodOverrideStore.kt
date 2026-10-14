package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback duty period overrides by code. */
interface FallbackDutyPeriodOverrideStore {
    fun find(code: String): FallbackDutyPeriodOverride?
    fun save(item: FallbackDutyPeriodOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDutyPeriodOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDutyPeriodOverrideStore(
    var items: MutableMap<String, FallbackDutyPeriodOverride>,
    var order: MutableList<String>,
) : FallbackDutyPeriodOverrideStore {
    override fun find(code: String): FallbackDutyPeriodOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDutyPeriodOverride) {
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

    override fun list(): MutableList<FallbackDutyPeriodOverride> {
        val out = mutableListOf<FallbackDutyPeriodOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDutyPeriodOverrideStore(): FallbackDutyPeriodOverrideStore {
    return MemoryFallbackDutyPeriodOverrideStore(
        mutableMapOf<String, FallbackDutyPeriodOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDutyPeriodOverrideStore(
    var inner: FallbackDutyPeriodOverrideStore,
    var cache: TtlCache<FallbackDutyPeriodOverride>,
    var hits: Int,
    var misses: Int,
) : FallbackDutyPeriodOverrideStore {
    override fun find(code: String): FallbackDutyPeriodOverride? {
        val hit = (cache.get(code) as FallbackDutyPeriodOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDutyPeriodOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDutyPeriodOverride> = inner.list()
}

fun newCachedFallbackDutyPeriodOverrideStore(inner: FallbackDutyPeriodOverrideStore, ttl: Int, clock: Clock): FallbackDutyPeriodOverrideStore {
    return CachedFallbackDutyPeriodOverrideStore(
        inner = inner,
        cache = newTtlCache<FallbackDutyPeriodOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback duty period overrides. Empty fields match all. */
data class FallbackDutyPeriodOverrideQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDutyPeriodOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDutyPeriodOverride>): MutableList<FallbackDutyPeriodOverride> {
        val matched = filter(items) { candidate: FallbackDutyPeriodOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDutyPeriodOverrides(store: FallbackDutyPeriodOverrideStore, query: FallbackDutyPeriodOverrideQuery): MutableList<FallbackDutyPeriodOverride> {
    return query.apply(store.list())
}
