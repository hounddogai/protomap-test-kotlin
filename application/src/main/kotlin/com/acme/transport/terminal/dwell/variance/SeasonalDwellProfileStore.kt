package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dwell profiles by code. */
interface SeasonalDwellProfileStore {
    fun find(code: String): SeasonalDwellProfile?
    fun save(item: SeasonalDwellProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDwellProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDwellProfileStore(
    var items: MutableMap<String, SeasonalDwellProfile>,
    var order: MutableList<String>,
) : SeasonalDwellProfileStore {
    override fun find(code: String): SeasonalDwellProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDwellProfile) {
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

    override fun list(): MutableList<SeasonalDwellProfile> {
        val out = mutableListOf<SeasonalDwellProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDwellProfileStore(): SeasonalDwellProfileStore {
    return MemorySeasonalDwellProfileStore(
        mutableMapOf<String, SeasonalDwellProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDwellProfileStore(
    var inner: SeasonalDwellProfileStore,
    var cache: TtlCache<SeasonalDwellProfile>,
    var hits: Int,
    var misses: Int,
) : SeasonalDwellProfileStore {
    override fun find(code: String): SeasonalDwellProfile? {
        val hit = (cache.get(code) as SeasonalDwellProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDwellProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDwellProfile> = inner.list()
}

fun newCachedSeasonalDwellProfileStore(inner: SeasonalDwellProfileStore, ttl: Int, clock: Clock): SeasonalDwellProfileStore {
    return CachedSeasonalDwellProfileStore(
        inner = inner,
        cache = newTtlCache<SeasonalDwellProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dwell profiles. Empty fields match all. */
data class SeasonalDwellProfileQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDwellProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<SeasonalDwellProfile>): MutableList<SeasonalDwellProfile> {
        val matched = filter(items) { candidate: SeasonalDwellProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDwellProfiles(store: SeasonalDwellProfileStore, query: SeasonalDwellProfileQuery): MutableList<SeasonalDwellProfile> {
    return query.apply(store.list())
}
