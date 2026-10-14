package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal trailer overrides by code. */
interface SeasonalTrailerOverrideStore {
    fun find(code: String): SeasonalTrailerOverride?
    fun save(item: SeasonalTrailerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerOverrideStore(
    var items: MutableMap<String, SeasonalTrailerOverride>,
    var order: MutableList<String>,
) : SeasonalTrailerOverrideStore {
    override fun find(code: String): SeasonalTrailerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerOverride) {
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

    override fun list(): MutableList<SeasonalTrailerOverride> {
        val out = mutableListOf<SeasonalTrailerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerOverrideStore(): SeasonalTrailerOverrideStore {
    return MemorySeasonalTrailerOverrideStore(
        mutableMapOf<String, SeasonalTrailerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerOverrideStore(
    var inner: SeasonalTrailerOverrideStore,
    var cache: TtlCache<SeasonalTrailerOverride>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerOverrideStore {
    override fun find(code: String): SeasonalTrailerOverride? {
        val hit = (cache.get(code) as SeasonalTrailerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerOverride> = inner.list()
}

fun newCachedSeasonalTrailerOverrideStore(inner: SeasonalTrailerOverrideStore, ttl: Int, clock: Clock): SeasonalTrailerOverrideStore {
    return CachedSeasonalTrailerOverrideStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer overrides. Empty fields match all. */
data class SeasonalTrailerOverrideQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerOverride>): MutableList<SeasonalTrailerOverride> {
        val matched = filter(items) { candidate: SeasonalTrailerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerOverrides(store: SeasonalTrailerOverrideStore, query: SeasonalTrailerOverrideQuery): MutableList<SeasonalTrailerOverride> {
    return query.apply(store.list())
}
