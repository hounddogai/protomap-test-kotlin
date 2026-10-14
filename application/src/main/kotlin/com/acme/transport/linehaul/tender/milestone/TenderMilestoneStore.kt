package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender milestones by code. */
interface TenderMilestoneStore {
    fun find(code: String): TenderMilestone?
    fun save(item: TenderMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderMilestoneStore(
    var items: MutableMap<String, TenderMilestone>,
    var order: MutableList<String>,
) : TenderMilestoneStore {
    override fun find(code: String): TenderMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderMilestone) {
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

    override fun list(): MutableList<TenderMilestone> {
        val out = mutableListOf<TenderMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderMilestoneStore(): TenderMilestoneStore {
    return MemoryTenderMilestoneStore(mutableMapOf<String, TenderMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderMilestoneStore(
    var inner: TenderMilestoneStore,
    var cache: TtlCache<TenderMilestone>,
    var hits: Int,
    var misses: Int,
) : TenderMilestoneStore {
    override fun find(code: String): TenderMilestone? {
        val hit = (cache.get(code) as TenderMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderMilestone> = inner.list()
}

fun newCachedTenderMilestoneStore(inner: TenderMilestoneStore, ttl: Int, clock: Clock): TenderMilestoneStore {
    return CachedTenderMilestoneStore(
        inner = inner,
        cache = newTtlCache<TenderMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender milestones. Empty fields match all. */
data class TenderMilestoneQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<TenderMilestone>): MutableList<TenderMilestone> {
        val matched = filter(items) { candidate: TenderMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderMilestones(store: TenderMilestoneStore, query: TenderMilestoneQuery): MutableList<TenderMilestone> {
    return query.apply(store.list())
}
