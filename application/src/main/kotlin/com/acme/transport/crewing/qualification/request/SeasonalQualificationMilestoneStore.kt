package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal qualification milestones by code. */
interface SeasonalQualificationMilestoneStore {
    fun find(code: String): SeasonalQualificationMilestone?
    fun save(item: SeasonalQualificationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalQualificationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalQualificationMilestoneStore(
    var items: MutableMap<String, SeasonalQualificationMilestone>,
    var order: MutableList<String>,
) : SeasonalQualificationMilestoneStore {
    override fun find(code: String): SeasonalQualificationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalQualificationMilestone) {
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

    override fun list(): MutableList<SeasonalQualificationMilestone> {
        val out = mutableListOf<SeasonalQualificationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalQualificationMilestoneStore(): SeasonalQualificationMilestoneStore {
    return MemorySeasonalQualificationMilestoneStore(
        mutableMapOf<String, SeasonalQualificationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalQualificationMilestoneStore(
    var inner: SeasonalQualificationMilestoneStore,
    var cache: TtlCache<SeasonalQualificationMilestone>,
    var hits: Int,
    var misses: Int,
) : SeasonalQualificationMilestoneStore {
    override fun find(code: String): SeasonalQualificationMilestone? {
        val hit = (cache.get(code) as SeasonalQualificationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalQualificationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalQualificationMilestone> = inner.list()
}

fun newCachedSeasonalQualificationMilestoneStore(inner: SeasonalQualificationMilestoneStore, ttl: Int, clock: Clock): SeasonalQualificationMilestoneStore {
    return CachedSeasonalQualificationMilestoneStore(
        inner = inner,
        cache = newTtlCache<SeasonalQualificationMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal qualification milestones. Empty fields match all. */
data class SeasonalQualificationMilestoneQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalQualificationMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalQualificationMilestone>): MutableList<SeasonalQualificationMilestone> {
        val matched = filter(items) { candidate: SeasonalQualificationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalQualificationMilestones(store: SeasonalQualificationMilestoneStore, query: SeasonalQualificationMilestoneQuery): MutableList<SeasonalQualificationMilestone> {
    return query.apply(store.list())
}
