package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt milestones by code. */
interface AttemptMilestoneStore {
    fun find(code: String): AttemptMilestone?
    fun save(item: AttemptMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptMilestoneStore(
    var items: MutableMap<String, AttemptMilestone>,
    var order: MutableList<String>,
) : AttemptMilestoneStore {
    override fun find(code: String): AttemptMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptMilestone) {
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

    override fun list(): MutableList<AttemptMilestone> {
        val out = mutableListOf<AttemptMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptMilestoneStore(): AttemptMilestoneStore {
    return MemoryAttemptMilestoneStore(mutableMapOf<String, AttemptMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptMilestoneStore(
    var inner: AttemptMilestoneStore,
    var cache: TtlCache<AttemptMilestone>,
    var hits: Int,
    var misses: Int,
) : AttemptMilestoneStore {
    override fun find(code: String): AttemptMilestone? {
        val hit = (cache.get(code) as AttemptMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptMilestone> = inner.list()
}

fun newCachedAttemptMilestoneStore(inner: AttemptMilestoneStore, ttl: Int, clock: Clock): AttemptMilestoneStore {
    return CachedAttemptMilestoneStore(
        inner = inner,
        cache = newTtlCache<AttemptMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt milestones. Empty fields match all. */
data class AttemptMilestoneQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptMilestone>): MutableList<AttemptMilestone> {
        val matched = filter(items) { candidate: AttemptMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptMilestones(store: AttemptMilestoneStore, query: AttemptMilestoneQuery): MutableList<AttemptMilestone> {
    return query.apply(store.list())
}
