package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling milestones by code. */
interface IdlingMilestoneStore {
    fun find(code: String): IdlingMilestone?
    fun save(item: IdlingMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingMilestoneStore(
    var items: MutableMap<String, IdlingMilestone>,
    var order: MutableList<String>,
) : IdlingMilestoneStore {
    override fun find(code: String): IdlingMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingMilestone) {
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

    override fun list(): MutableList<IdlingMilestone> {
        val out = mutableListOf<IdlingMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingMilestoneStore(): IdlingMilestoneStore {
    return MemoryIdlingMilestoneStore(mutableMapOf<String, IdlingMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingMilestoneStore(
    var inner: IdlingMilestoneStore,
    var cache: TtlCache<IdlingMilestone>,
    var hits: Int,
    var misses: Int,
) : IdlingMilestoneStore {
    override fun find(code: String): IdlingMilestone? {
        val hit = (cache.get(code) as IdlingMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingMilestone> = inner.list()
}

fun newCachedIdlingMilestoneStore(inner: IdlingMilestoneStore, ttl: Int, clock: Clock): IdlingMilestoneStore {
    return CachedIdlingMilestoneStore(
        inner = inner,
        cache = newTtlCache<IdlingMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling milestones. Empty fields match all. */
data class IdlingMilestoneQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingMilestone): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingMilestone>): MutableList<IdlingMilestone> {
        val matched = filter(items) { candidate: IdlingMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingMilestones(store: IdlingMilestoneStore, query: IdlingMilestoneQuery): MutableList<IdlingMilestone> {
    return query.apply(store.list())
}
