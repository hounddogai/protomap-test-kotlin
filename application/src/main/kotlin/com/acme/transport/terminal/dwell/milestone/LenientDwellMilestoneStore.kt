package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell milestones by code. */
interface LenientDwellMilestoneStore {
    fun find(code: String): LenientDwellMilestone?
    fun save(item: LenientDwellMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellMilestoneStore(
    var items: MutableMap<String, LenientDwellMilestone>,
    var order: MutableList<String>,
) : LenientDwellMilestoneStore {
    override fun find(code: String): LenientDwellMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellMilestone) {
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

    override fun list(): MutableList<LenientDwellMilestone> {
        val out = mutableListOf<LenientDwellMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellMilestoneStore(): LenientDwellMilestoneStore {
    return MemoryLenientDwellMilestoneStore(
        mutableMapOf<String, LenientDwellMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellMilestoneStore(
    var inner: LenientDwellMilestoneStore,
    var cache: TtlCache<LenientDwellMilestone>,
    var hits: Int,
    var misses: Int,
) : LenientDwellMilestoneStore {
    override fun find(code: String): LenientDwellMilestone? {
        val hit = (cache.get(code) as LenientDwellMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellMilestone> = inner.list()
}

fun newCachedLenientDwellMilestoneStore(inner: LenientDwellMilestoneStore, ttl: Int, clock: Clock): LenientDwellMilestoneStore {
    return CachedLenientDwellMilestoneStore(
        inner = inner,
        cache = newTtlCache<LenientDwellMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell milestones. Empty fields match all. */
data class LenientDwellMilestoneQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientDwellMilestone>): MutableList<LenientDwellMilestone> {
        val matched = filter(items) { candidate: LenientDwellMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellMilestones(store: LenientDwellMilestoneStore, query: LenientDwellMilestoneQuery): MutableList<LenientDwellMilestone> {
    return query.apply(store.list())
}
