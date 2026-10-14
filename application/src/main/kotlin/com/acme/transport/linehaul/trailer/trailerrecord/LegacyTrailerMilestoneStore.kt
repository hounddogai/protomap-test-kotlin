package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy trailer milestones by code. */
interface LegacyTrailerMilestoneStore {
    fun find(code: String): LegacyTrailerMilestone?
    fun save(item: LegacyTrailerMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerMilestoneStore(
    var items: MutableMap<String, LegacyTrailerMilestone>,
    var order: MutableList<String>,
) : LegacyTrailerMilestoneStore {
    override fun find(code: String): LegacyTrailerMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerMilestone) {
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

    override fun list(): MutableList<LegacyTrailerMilestone> {
        val out = mutableListOf<LegacyTrailerMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerMilestoneStore(): LegacyTrailerMilestoneStore {
    return MemoryLegacyTrailerMilestoneStore(
        mutableMapOf<String, LegacyTrailerMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerMilestoneStore(
    var inner: LegacyTrailerMilestoneStore,
    var cache: TtlCache<LegacyTrailerMilestone>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerMilestoneStore {
    override fun find(code: String): LegacyTrailerMilestone? {
        val hit = (cache.get(code) as LegacyTrailerMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerMilestone> = inner.list()
}

fun newCachedLegacyTrailerMilestoneStore(inner: LegacyTrailerMilestoneStore, ttl: Int, clock: Clock): LegacyTrailerMilestoneStore {
    return CachedLegacyTrailerMilestoneStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer milestones. Empty fields match all. */
data class LegacyTrailerMilestoneQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerMilestone>): MutableList<LegacyTrailerMilestone> {
        val matched = filter(items) { candidate: LegacyTrailerMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerMilestones(store: LegacyTrailerMilestoneStore, query: LegacyTrailerMilestoneQuery): MutableList<LegacyTrailerMilestone> {
    return query.apply(store.list())
}
