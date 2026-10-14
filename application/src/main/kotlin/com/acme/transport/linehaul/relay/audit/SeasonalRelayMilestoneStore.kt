package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal relay milestones by code. */
interface SeasonalRelayMilestoneStore {
    fun find(code: String): SeasonalRelayMilestone?
    fun save(item: SeasonalRelayMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRelayMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRelayMilestoneStore(
    var items: MutableMap<String, SeasonalRelayMilestone>,
    var order: MutableList<String>,
) : SeasonalRelayMilestoneStore {
    override fun find(code: String): SeasonalRelayMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRelayMilestone) {
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

    override fun list(): MutableList<SeasonalRelayMilestone> {
        val out = mutableListOf<SeasonalRelayMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRelayMilestoneStore(): SeasonalRelayMilestoneStore {
    return MemorySeasonalRelayMilestoneStore(
        mutableMapOf<String, SeasonalRelayMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRelayMilestoneStore(
    var inner: SeasonalRelayMilestoneStore,
    var cache: TtlCache<SeasonalRelayMilestone>,
    var hits: Int,
    var misses: Int,
) : SeasonalRelayMilestoneStore {
    override fun find(code: String): SeasonalRelayMilestone? {
        val hit = (cache.get(code) as SeasonalRelayMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRelayMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRelayMilestone> = inner.list()
}

fun newCachedSeasonalRelayMilestoneStore(inner: SeasonalRelayMilestoneStore, ttl: Int, clock: Clock): SeasonalRelayMilestoneStore {
    return CachedSeasonalRelayMilestoneStore(
        inner = inner,
        cache = newTtlCache<SeasonalRelayMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal relay milestones. Empty fields match all. */
data class SeasonalRelayMilestoneQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRelayMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRelayMilestone>): MutableList<SeasonalRelayMilestone> {
        val matched = filter(items) { candidate: SeasonalRelayMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRelayMilestones(store: SeasonalRelayMilestoneStore, query: SeasonalRelayMilestoneQuery): MutableList<SeasonalRelayMilestone> {
    return query.apply(store.list())
}
