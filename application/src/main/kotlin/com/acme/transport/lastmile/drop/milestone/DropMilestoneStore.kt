package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop milestones by code. */
interface DropMilestoneStore {
    fun find(code: String): DropMilestone?
    fun save(item: DropMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropMilestoneStore(
    var items: MutableMap<String, DropMilestone>,
    var order: MutableList<String>,
) : DropMilestoneStore {
    override fun find(code: String): DropMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropMilestone) {
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

    override fun list(): MutableList<DropMilestone> {
        val out = mutableListOf<DropMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropMilestoneStore(): DropMilestoneStore {
    return MemoryDropMilestoneStore(mutableMapOf<String, DropMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropMilestoneStore(
    var inner: DropMilestoneStore,
    var cache: TtlCache<DropMilestone>,
    var hits: Int,
    var misses: Int,
) : DropMilestoneStore {
    override fun find(code: String): DropMilestone? {
        val hit = (cache.get(code) as DropMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropMilestone> = inner.list()
}

fun newCachedDropMilestoneStore(inner: DropMilestoneStore, ttl: Int, clock: Clock): DropMilestoneStore {
    return CachedDropMilestoneStore(
        inner = inner,
        cache = newTtlCache<DropMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop milestones. Empty fields match all. */
data class DropMilestoneQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropMilestone>): MutableList<DropMilestone> {
        val matched = filter(items) { candidate: DropMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropMilestones(store: DropMilestoneStore, query: DropMilestoneQuery): MutableList<DropMilestone> {
    return query.apply(store.list())
}
