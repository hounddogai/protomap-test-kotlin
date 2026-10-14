package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global redelivery milestones by code. */
interface GlobalRedeliveryMilestoneStore {
    fun find(code: String): GlobalRedeliveryMilestone?
    fun save(item: GlobalRedeliveryMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRedeliveryMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRedeliveryMilestoneStore(
    var items: MutableMap<String, GlobalRedeliveryMilestone>,
    var order: MutableList<String>,
) : GlobalRedeliveryMilestoneStore {
    override fun find(code: String): GlobalRedeliveryMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRedeliveryMilestone) {
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

    override fun list(): MutableList<GlobalRedeliveryMilestone> {
        val out = mutableListOf<GlobalRedeliveryMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRedeliveryMilestoneStore(): GlobalRedeliveryMilestoneStore {
    return MemoryGlobalRedeliveryMilestoneStore(
        mutableMapOf<String, GlobalRedeliveryMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRedeliveryMilestoneStore(
    var inner: GlobalRedeliveryMilestoneStore,
    var cache: TtlCache<GlobalRedeliveryMilestone>,
    var hits: Int,
    var misses: Int,
) : GlobalRedeliveryMilestoneStore {
    override fun find(code: String): GlobalRedeliveryMilestone? {
        val hit = (cache.get(code) as GlobalRedeliveryMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRedeliveryMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRedeliveryMilestone> = inner.list()
}

fun newCachedGlobalRedeliveryMilestoneStore(inner: GlobalRedeliveryMilestoneStore, ttl: Int, clock: Clock): GlobalRedeliveryMilestoneStore {
    return CachedGlobalRedeliveryMilestoneStore(
        inner = inner,
        cache = newTtlCache<GlobalRedeliveryMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global redelivery milestones. Empty fields match all. */
data class GlobalRedeliveryMilestoneQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRedeliveryMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRedeliveryMilestone>): MutableList<GlobalRedeliveryMilestone> {
        val matched = filter(items) { candidate: GlobalRedeliveryMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRedeliveryMilestones(store: GlobalRedeliveryMilestoneStore, query: GlobalRedeliveryMilestoneQuery): MutableList<GlobalRedeliveryMilestone> {
    return query.apply(store.list())
}
