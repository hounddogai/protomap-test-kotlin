package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback attempt milestones by code. */
interface FallbackAttemptMilestoneStore {
    fun find(code: String): FallbackAttemptMilestone?
    fun save(item: FallbackAttemptMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAttemptMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAttemptMilestoneStore(
    var items: MutableMap<String, FallbackAttemptMilestone>,
    var order: MutableList<String>,
) : FallbackAttemptMilestoneStore {
    override fun find(code: String): FallbackAttemptMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAttemptMilestone) {
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

    override fun list(): MutableList<FallbackAttemptMilestone> {
        val out = mutableListOf<FallbackAttemptMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAttemptMilestoneStore(): FallbackAttemptMilestoneStore {
    return MemoryFallbackAttemptMilestoneStore(
        mutableMapOf<String, FallbackAttemptMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAttemptMilestoneStore(
    var inner: FallbackAttemptMilestoneStore,
    var cache: TtlCache<FallbackAttemptMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackAttemptMilestoneStore {
    override fun find(code: String): FallbackAttemptMilestone? {
        val hit = (cache.get(code) as FallbackAttemptMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAttemptMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAttemptMilestone> = inner.list()
}

fun newCachedFallbackAttemptMilestoneStore(inner: FallbackAttemptMilestoneStore, ttl: Int, clock: Clock): FallbackAttemptMilestoneStore {
    return CachedFallbackAttemptMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackAttemptMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback attempt milestones. Empty fields match all. */
data class FallbackAttemptMilestoneQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAttemptMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackAttemptMilestone>): MutableList<FallbackAttemptMilestone> {
        val matched = filter(items) { candidate: FallbackAttemptMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAttemptMilestones(store: FallbackAttemptMilestoneStore, query: FallbackAttemptMilestoneQuery): MutableList<FallbackAttemptMilestone> {
    return query.apply(store.list())
}
