package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery groups by code. */
interface SeasonalRedeliveryGroupStore {
    fun find(code: String): SeasonalRedeliveryGroup?
    fun save(item: SeasonalRedeliveryGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliveryGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryGroupStore(
    var items: MutableMap<String, SeasonalRedeliveryGroup>,
    var order: MutableList<String>,
) : SeasonalRedeliveryGroupStore {
    override fun find(code: String): SeasonalRedeliveryGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliveryGroup) {
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

    override fun list(): MutableList<SeasonalRedeliveryGroup> {
        val out = mutableListOf<SeasonalRedeliveryGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryGroupStore(): SeasonalRedeliveryGroupStore {
    return MemorySeasonalRedeliveryGroupStore(
        mutableMapOf<String, SeasonalRedeliveryGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryGroupStore(
    var inner: SeasonalRedeliveryGroupStore,
    var cache: TtlCache<SeasonalRedeliveryGroup>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryGroupStore {
    override fun find(code: String): SeasonalRedeliveryGroup? {
        val hit = (cache.get(code) as SeasonalRedeliveryGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliveryGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliveryGroup> = inner.list()
}

fun newCachedSeasonalRedeliveryGroupStore(inner: SeasonalRedeliveryGroupStore, ttl: Int, clock: Clock): SeasonalRedeliveryGroupStore {
    return CachedSeasonalRedeliveryGroupStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliveryGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery groups. Empty fields match all. */
data class SeasonalRedeliveryGroupQuery(
    var source: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliveryGroup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SeasonalRedeliveryGroup>): MutableList<SeasonalRedeliveryGroup> {
        val matched = filter(items) { candidate: SeasonalRedeliveryGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliveryGroups(store: SeasonalRedeliveryGroupStore, query: SeasonalRedeliveryGroupQuery): MutableList<SeasonalRedeliveryGroup> {
    return query.apply(store.list())
}
