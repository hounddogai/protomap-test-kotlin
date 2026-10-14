package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default service area milestones by code. */
interface DefaultServiceAreaMilestoneStore {
    fun find(code: String): DefaultServiceAreaMilestone?
    fun save(item: DefaultServiceAreaMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServiceAreaMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServiceAreaMilestoneStore(
    var items: MutableMap<String, DefaultServiceAreaMilestone>,
    var order: MutableList<String>,
) : DefaultServiceAreaMilestoneStore {
    override fun find(code: String): DefaultServiceAreaMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServiceAreaMilestone) {
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

    override fun list(): MutableList<DefaultServiceAreaMilestone> {
        val out = mutableListOf<DefaultServiceAreaMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServiceAreaMilestoneStore(): DefaultServiceAreaMilestoneStore {
    return MemoryDefaultServiceAreaMilestoneStore(
        mutableMapOf<String, DefaultServiceAreaMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServiceAreaMilestoneStore(
    var inner: DefaultServiceAreaMilestoneStore,
    var cache: TtlCache<DefaultServiceAreaMilestone>,
    var hits: Int,
    var misses: Int,
) : DefaultServiceAreaMilestoneStore {
    override fun find(code: String): DefaultServiceAreaMilestone? {
        val hit = (cache.get(code) as DefaultServiceAreaMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServiceAreaMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServiceAreaMilestone> = inner.list()
}

fun newCachedDefaultServiceAreaMilestoneStore(inner: DefaultServiceAreaMilestoneStore, ttl: Int, clock: Clock): DefaultServiceAreaMilestoneStore {
    return CachedDefaultServiceAreaMilestoneStore(
        inner = inner,
        cache = newTtlCache<DefaultServiceAreaMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service area milestones. Empty fields match all. */
data class DefaultServiceAreaMilestoneQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServiceAreaMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServiceAreaMilestone>): MutableList<DefaultServiceAreaMilestone> {
        val matched = filter(items) { candidate: DefaultServiceAreaMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServiceAreaMilestones(store: DefaultServiceAreaMilestoneStore, query: DefaultServiceAreaMilestoneQuery): MutableList<DefaultServiceAreaMilestone> {
    return query.apply(store.list())
}
