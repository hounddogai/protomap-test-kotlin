package com.acme.transport.crewing.roster.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster adjustments by code. */
interface RosterAdjustmentStore {
    fun find(code: String): RosterAdjustment?
    fun save(item: RosterAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterAdjustmentStore(
    var items: MutableMap<String, RosterAdjustment>,
    var order: MutableList<String>,
) : RosterAdjustmentStore {
    override fun find(code: String): RosterAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterAdjustment) {
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

    override fun list(): MutableList<RosterAdjustment> {
        val out = mutableListOf<RosterAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterAdjustmentStore(): RosterAdjustmentStore {
    return MemoryRosterAdjustmentStore(mutableMapOf<String, RosterAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterAdjustmentStore(
    var inner: RosterAdjustmentStore,
    var cache: TtlCache<RosterAdjustment>,
    var hits: Int,
    var misses: Int,
) : RosterAdjustmentStore {
    override fun find(code: String): RosterAdjustment? {
        val hit = (cache.get(code) as RosterAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterAdjustment> = inner.list()
}

fun newCachedRosterAdjustmentStore(inner: RosterAdjustmentStore, ttl: Int, clock: Clock): RosterAdjustmentStore {
    return CachedRosterAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RosterAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster adjustments. Empty fields match all. */
data class RosterAdjustmentQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RosterAdjustment>): MutableList<RosterAdjustment> {
        val matched = filter(items) { candidate: RosterAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterAdjustments(store: RosterAdjustmentStore, query: RosterAdjustmentQuery): MutableList<RosterAdjustment> {
    return query.apply(store.list())
}
