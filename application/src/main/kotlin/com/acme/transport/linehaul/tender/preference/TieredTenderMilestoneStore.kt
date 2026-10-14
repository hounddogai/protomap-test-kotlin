package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered tender milestones by code. */
interface TieredTenderMilestoneStore {
    fun find(code: String): TieredTenderMilestone?
    fun save(item: TieredTenderMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTenderMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTenderMilestoneStore(
    var items: MutableMap<String, TieredTenderMilestone>,
    var order: MutableList<String>,
) : TieredTenderMilestoneStore {
    override fun find(code: String): TieredTenderMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTenderMilestone) {
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

    override fun list(): MutableList<TieredTenderMilestone> {
        val out = mutableListOf<TieredTenderMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTenderMilestoneStore(): TieredTenderMilestoneStore {
    return MemoryTieredTenderMilestoneStore(
        mutableMapOf<String, TieredTenderMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTenderMilestoneStore(
    var inner: TieredTenderMilestoneStore,
    var cache: TtlCache<TieredTenderMilestone>,
    var hits: Int,
    var misses: Int,
) : TieredTenderMilestoneStore {
    override fun find(code: String): TieredTenderMilestone? {
        val hit = (cache.get(code) as TieredTenderMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTenderMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTenderMilestone> = inner.list()
}

fun newCachedTieredTenderMilestoneStore(inner: TieredTenderMilestoneStore, ttl: Int, clock: Clock): TieredTenderMilestoneStore {
    return CachedTieredTenderMilestoneStore(
        inner = inner,
        cache = newTtlCache<TieredTenderMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tender milestones. Empty fields match all. */
data class TieredTenderMilestoneQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTenderMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTenderMilestone>): MutableList<TieredTenderMilestone> {
        val matched = filter(items) { candidate: TieredTenderMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTenderMilestones(store: TieredTenderMilestoneStore, query: TieredTenderMilestoneQuery): MutableList<TieredTenderMilestone> {
    return query.apply(store.list())
}
