package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict drop milestones by code. */
interface StrictDropMilestoneStore {
    fun find(code: String): StrictDropMilestone?
    fun save(item: StrictDropMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDropMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDropMilestoneStore(
    var items: MutableMap<String, StrictDropMilestone>,
    var order: MutableList<String>,
) : StrictDropMilestoneStore {
    override fun find(code: String): StrictDropMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDropMilestone) {
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

    override fun list(): MutableList<StrictDropMilestone> {
        val out = mutableListOf<StrictDropMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDropMilestoneStore(): StrictDropMilestoneStore {
    return MemoryStrictDropMilestoneStore(mutableMapOf<String, StrictDropMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDropMilestoneStore(
    var inner: StrictDropMilestoneStore,
    var cache: TtlCache<StrictDropMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictDropMilestoneStore {
    override fun find(code: String): StrictDropMilestone? {
        val hit = (cache.get(code) as StrictDropMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDropMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDropMilestone> = inner.list()
}

fun newCachedStrictDropMilestoneStore(inner: StrictDropMilestoneStore, ttl: Int, clock: Clock): StrictDropMilestoneStore {
    return CachedStrictDropMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictDropMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict drop milestones. Empty fields match all. */
data class StrictDropMilestoneQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDropMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDropMilestone>): MutableList<StrictDropMilestone> {
        val matched = filter(items) { candidate: StrictDropMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDropMilestones(store: StrictDropMilestoneStore, query: StrictDropMilestoneQuery): MutableList<StrictDropMilestone> {
    return query.apply(store.list())
}
