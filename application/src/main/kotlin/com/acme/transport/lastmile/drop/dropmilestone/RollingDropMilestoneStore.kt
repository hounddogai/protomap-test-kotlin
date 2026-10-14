package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling drop milestones by code. */
interface RollingDropMilestoneStore {
    fun find(code: String): RollingDropMilestone?
    fun save(item: RollingDropMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDropMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDropMilestoneStore(
    var items: MutableMap<String, RollingDropMilestone>,
    var order: MutableList<String>,
) : RollingDropMilestoneStore {
    override fun find(code: String): RollingDropMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDropMilestone) {
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

    override fun list(): MutableList<RollingDropMilestone> {
        val out = mutableListOf<RollingDropMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDropMilestoneStore(): RollingDropMilestoneStore {
    return MemoryRollingDropMilestoneStore(
        mutableMapOf<String, RollingDropMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDropMilestoneStore(
    var inner: RollingDropMilestoneStore,
    var cache: TtlCache<RollingDropMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingDropMilestoneStore {
    override fun find(code: String): RollingDropMilestone? {
        val hit = (cache.get(code) as RollingDropMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDropMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDropMilestone> = inner.list()
}

fun newCachedRollingDropMilestoneStore(inner: RollingDropMilestoneStore, ttl: Int, clock: Clock): RollingDropMilestoneStore {
    return CachedRollingDropMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingDropMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling drop milestones. Empty fields match all. */
data class RollingDropMilestoneQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDropMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDropMilestone>): MutableList<RollingDropMilestone> {
        val matched = filter(items) { candidate: RollingDropMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDropMilestones(store: RollingDropMilestoneStore, query: RollingDropMilestoneQuery): MutableList<RollingDropMilestone> {
    return query.apply(store.list())
}
