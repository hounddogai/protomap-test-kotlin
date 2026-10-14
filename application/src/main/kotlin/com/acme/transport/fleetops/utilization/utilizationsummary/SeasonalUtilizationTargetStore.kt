package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal utilization targets by code. */
interface SeasonalUtilizationTargetStore {
    fun find(code: String): SeasonalUtilizationTarget?
    fun save(item: SeasonalUtilizationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalUtilizationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalUtilizationTargetStore(
    var items: MutableMap<String, SeasonalUtilizationTarget>,
    var order: MutableList<String>,
) : SeasonalUtilizationTargetStore {
    override fun find(code: String): SeasonalUtilizationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalUtilizationTarget) {
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

    override fun list(): MutableList<SeasonalUtilizationTarget> {
        val out = mutableListOf<SeasonalUtilizationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalUtilizationTargetStore(): SeasonalUtilizationTargetStore {
    return MemorySeasonalUtilizationTargetStore(
        mutableMapOf<String, SeasonalUtilizationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalUtilizationTargetStore(
    var inner: SeasonalUtilizationTargetStore,
    var cache: TtlCache<SeasonalUtilizationTarget>,
    var hits: Int,
    var misses: Int,
) : SeasonalUtilizationTargetStore {
    override fun find(code: String): SeasonalUtilizationTarget? {
        val hit = (cache.get(code) as SeasonalUtilizationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalUtilizationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalUtilizationTarget> = inner.list()
}

fun newCachedSeasonalUtilizationTargetStore(inner: SeasonalUtilizationTargetStore, ttl: Int, clock: Clock): SeasonalUtilizationTargetStore {
    return CachedSeasonalUtilizationTargetStore(
        inner = inner,
        cache = newTtlCache<SeasonalUtilizationTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal utilization targets. Empty fields match all. */
data class SeasonalUtilizationTargetQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalUtilizationTarget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalUtilizationTarget>): MutableList<SeasonalUtilizationTarget> {
        val matched = filter(items) { candidate: SeasonalUtilizationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalUtilizationTargets(store: SeasonalUtilizationTargetStore, query: SeasonalUtilizationTargetQuery): MutableList<SeasonalUtilizationTarget> {
    return query.apply(store.list())
}
