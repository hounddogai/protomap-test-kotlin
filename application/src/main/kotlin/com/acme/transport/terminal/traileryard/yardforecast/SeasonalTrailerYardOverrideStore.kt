package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard overrides by code. */
interface SeasonalTrailerYardOverrideStore {
    fun find(code: String): SeasonalTrailerYardOverride?
    fun save(item: SeasonalTrailerYardOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardOverrideStore(
    var items: MutableMap<String, SeasonalTrailerYardOverride>,
    var order: MutableList<String>,
) : SeasonalTrailerYardOverrideStore {
    override fun find(code: String): SeasonalTrailerYardOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardOverride) {
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

    override fun list(): MutableList<SeasonalTrailerYardOverride> {
        val out = mutableListOf<SeasonalTrailerYardOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardOverrideStore(): SeasonalTrailerYardOverrideStore {
    return MemorySeasonalTrailerYardOverrideStore(
        mutableMapOf<String, SeasonalTrailerYardOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardOverrideStore(
    var inner: SeasonalTrailerYardOverrideStore,
    var cache: TtlCache<SeasonalTrailerYardOverride>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardOverrideStore {
    override fun find(code: String): SeasonalTrailerYardOverride? {
        val hit = (cache.get(code) as SeasonalTrailerYardOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardOverride> = inner.list()
}

fun newCachedSeasonalTrailerYardOverrideStore(inner: SeasonalTrailerYardOverrideStore, ttl: Int, clock: Clock): SeasonalTrailerYardOverrideStore {
    return CachedSeasonalTrailerYardOverrideStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard overrides. Empty fields match all. */
data class SeasonalTrailerYardOverrideQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardOverride): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<SeasonalTrailerYardOverride>): MutableList<SeasonalTrailerYardOverride> {
        val matched = filter(items) { candidate: SeasonalTrailerYardOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardOverrides(store: SeasonalTrailerYardOverrideStore, query: SeasonalTrailerYardOverrideQuery): MutableList<SeasonalTrailerYardOverride> {
    return query.apply(store.list())
}
