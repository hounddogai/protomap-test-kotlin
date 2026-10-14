package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal relay baselines by code. */
interface SeasonalRelayBaselineStore {
    fun find(code: String): SeasonalRelayBaseline?
    fun save(item: SeasonalRelayBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRelayBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRelayBaselineStore(
    var items: MutableMap<String, SeasonalRelayBaseline>,
    var order: MutableList<String>,
) : SeasonalRelayBaselineStore {
    override fun find(code: String): SeasonalRelayBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRelayBaseline) {
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

    override fun list(): MutableList<SeasonalRelayBaseline> {
        val out = mutableListOf<SeasonalRelayBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRelayBaselineStore(): SeasonalRelayBaselineStore {
    return MemorySeasonalRelayBaselineStore(
        mutableMapOf<String, SeasonalRelayBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRelayBaselineStore(
    var inner: SeasonalRelayBaselineStore,
    var cache: TtlCache<SeasonalRelayBaseline>,
    var hits: Int,
    var misses: Int,
) : SeasonalRelayBaselineStore {
    override fun find(code: String): SeasonalRelayBaseline? {
        val hit = (cache.get(code) as SeasonalRelayBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRelayBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRelayBaseline> = inner.list()
}

fun newCachedSeasonalRelayBaselineStore(inner: SeasonalRelayBaselineStore, ttl: Int, clock: Clock): SeasonalRelayBaselineStore {
    return CachedSeasonalRelayBaselineStore(
        inner = inner,
        cache = newTtlCache<SeasonalRelayBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal relay baselines. Empty fields match all. */
data class SeasonalRelayBaselineQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRelayBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRelayBaseline>): MutableList<SeasonalRelayBaseline> {
        val matched = filter(items) { candidate: SeasonalRelayBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRelayBaselines(store: SeasonalRelayBaselineStore, query: SeasonalRelayBaselineQuery): MutableList<SeasonalRelayBaseline> {
    return query.apply(store.list())
}
