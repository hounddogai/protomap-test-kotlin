package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel milestones by code. */
interface DefaultParcelMilestoneStore {
    fun find(code: String): DefaultParcelMilestone?
    fun save(item: DefaultParcelMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelMilestoneStore(
    var items: MutableMap<String, DefaultParcelMilestone>,
    var order: MutableList<String>,
) : DefaultParcelMilestoneStore {
    override fun find(code: String): DefaultParcelMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelMilestone) {
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

    override fun list(): MutableList<DefaultParcelMilestone> {
        val out = mutableListOf<DefaultParcelMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelMilestoneStore(): DefaultParcelMilestoneStore {
    return MemoryDefaultParcelMilestoneStore(
        mutableMapOf<String, DefaultParcelMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelMilestoneStore(
    var inner: DefaultParcelMilestoneStore,
    var cache: TtlCache<DefaultParcelMilestone>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelMilestoneStore {
    override fun find(code: String): DefaultParcelMilestone? {
        val hit = (cache.get(code) as DefaultParcelMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelMilestone> = inner.list()
}

fun newCachedDefaultParcelMilestoneStore(inner: DefaultParcelMilestoneStore, ttl: Int, clock: Clock): DefaultParcelMilestoneStore {
    return CachedDefaultParcelMilestoneStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel milestones. Empty fields match all. */
data class DefaultParcelMilestoneQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelMilestone>): MutableList<DefaultParcelMilestone> {
        val matched = filter(items) { candidate: DefaultParcelMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelMilestones(store: DefaultParcelMilestoneStore, query: DefaultParcelMilestoneQuery): MutableList<DefaultParcelMilestone> {
    return query.apply(store.list())
}
