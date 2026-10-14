package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer milestones by code. */
interface TrailerMilestoneStore {
    fun find(code: String): TrailerMilestone?
    fun save(item: TrailerMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerMilestoneStore(
    var items: MutableMap<String, TrailerMilestone>,
    var order: MutableList<String>,
) : TrailerMilestoneStore {
    override fun find(code: String): TrailerMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerMilestone) {
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

    override fun list(): MutableList<TrailerMilestone> {
        val out = mutableListOf<TrailerMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerMilestoneStore(): TrailerMilestoneStore {
    return MemoryTrailerMilestoneStore(mutableMapOf<String, TrailerMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerMilestoneStore(
    var inner: TrailerMilestoneStore,
    var cache: TtlCache<TrailerMilestone>,
    var hits: Int,
    var misses: Int,
) : TrailerMilestoneStore {
    override fun find(code: String): TrailerMilestone? {
        val hit = (cache.get(code) as TrailerMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerMilestone> = inner.list()
}

fun newCachedTrailerMilestoneStore(inner: TrailerMilestoneStore, ttl: Int, clock: Clock): TrailerMilestoneStore {
    return CachedTrailerMilestoneStore(
        inner = inner,
        cache = newTtlCache<TrailerMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer milestones. Empty fields match all. */
data class TrailerMilestoneQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerMilestone>): MutableList<TrailerMilestone> {
        val matched = filter(items) { candidate: TrailerMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerMilestones(store: TrailerMilestoneStore, query: TrailerMilestoneQuery): MutableList<TrailerMilestone> {
    return query.apply(store.list())
}
