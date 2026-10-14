package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary camera event milestones by code. */
interface PrimaryCameraEventMilestoneStore {
    fun find(code: String): PrimaryCameraEventMilestone?
    fun save(item: PrimaryCameraEventMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCameraEventMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCameraEventMilestoneStore(
    var items: MutableMap<String, PrimaryCameraEventMilestone>,
    var order: MutableList<String>,
) : PrimaryCameraEventMilestoneStore {
    override fun find(code: String): PrimaryCameraEventMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCameraEventMilestone) {
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

    override fun list(): MutableList<PrimaryCameraEventMilestone> {
        val out = mutableListOf<PrimaryCameraEventMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCameraEventMilestoneStore(): PrimaryCameraEventMilestoneStore {
    return MemoryPrimaryCameraEventMilestoneStore(
        mutableMapOf<String, PrimaryCameraEventMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCameraEventMilestoneStore(
    var inner: PrimaryCameraEventMilestoneStore,
    var cache: TtlCache<PrimaryCameraEventMilestone>,
    var hits: Int,
    var misses: Int,
) : PrimaryCameraEventMilestoneStore {
    override fun find(code: String): PrimaryCameraEventMilestone? {
        val hit = (cache.get(code) as PrimaryCameraEventMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCameraEventMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCameraEventMilestone> = inner.list()
}

fun newCachedPrimaryCameraEventMilestoneStore(inner: PrimaryCameraEventMilestoneStore, ttl: Int, clock: Clock): PrimaryCameraEventMilestoneStore {
    return CachedPrimaryCameraEventMilestoneStore(
        inner = inner,
        cache = newTtlCache<PrimaryCameraEventMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary camera event milestones. Empty fields match all. */
data class PrimaryCameraEventMilestoneQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCameraEventMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCameraEventMilestone>): MutableList<PrimaryCameraEventMilestone> {
        val matched = filter(items) { candidate: PrimaryCameraEventMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCameraEventMilestones(store: PrimaryCameraEventMilestoneStore, query: PrimaryCameraEventMilestoneQuery): MutableList<PrimaryCameraEventMilestone> {
    return query.apply(store.list())
}
