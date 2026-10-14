package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal clearance profiles by code. */
interface SeasonalClearanceProfileStore {
    fun find(code: String): SeasonalClearanceProfile?
    fun save(item: SeasonalClearanceProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalClearanceProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalClearanceProfileStore(
    var items: MutableMap<String, SeasonalClearanceProfile>,
    var order: MutableList<String>,
) : SeasonalClearanceProfileStore {
    override fun find(code: String): SeasonalClearanceProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalClearanceProfile) {
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

    override fun list(): MutableList<SeasonalClearanceProfile> {
        val out = mutableListOf<SeasonalClearanceProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalClearanceProfileStore(): SeasonalClearanceProfileStore {
    return MemorySeasonalClearanceProfileStore(
        mutableMapOf<String, SeasonalClearanceProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalClearanceProfileStore(
    var inner: SeasonalClearanceProfileStore,
    var cache: TtlCache<SeasonalClearanceProfile>,
    var hits: Int,
    var misses: Int,
) : SeasonalClearanceProfileStore {
    override fun find(code: String): SeasonalClearanceProfile? {
        val hit = (cache.get(code) as SeasonalClearanceProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalClearanceProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalClearanceProfile> = inner.list()
}

fun newCachedSeasonalClearanceProfileStore(inner: SeasonalClearanceProfileStore, ttl: Int, clock: Clock): SeasonalClearanceProfileStore {
    return CachedSeasonalClearanceProfileStore(
        inner = inner,
        cache = newTtlCache<SeasonalClearanceProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal clearance profiles. Empty fields match all. */
data class SeasonalClearanceProfileQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalClearanceProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SeasonalClearanceProfile>): MutableList<SeasonalClearanceProfile> {
        val matched = filter(items) { candidate: SeasonalClearanceProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalClearanceProfiles(store: SeasonalClearanceProfileStore, query: SeasonalClearanceProfileQuery): MutableList<SeasonalClearanceProfile> {
    return query.apply(store.list())
}
