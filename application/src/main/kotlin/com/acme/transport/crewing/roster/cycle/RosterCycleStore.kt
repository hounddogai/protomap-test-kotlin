package com.acme.transport.crewing.roster.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster cycles by code. */
interface RosterCycleStore {
    fun find(code: String): RosterCycle?
    fun save(item: RosterCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterCycleStore(
    var items: MutableMap<String, RosterCycle>,
    var order: MutableList<String>,
) : RosterCycleStore {
    override fun find(code: String): RosterCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterCycle) {
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

    override fun list(): MutableList<RosterCycle> {
        val out = mutableListOf<RosterCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterCycleStore(): RosterCycleStore {
    return MemoryRosterCycleStore(mutableMapOf<String, RosterCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterCycleStore(
    var inner: RosterCycleStore,
    var cache: TtlCache<RosterCycle>,
    var hits: Int,
    var misses: Int,
) : RosterCycleStore {
    override fun find(code: String): RosterCycle? {
        val hit = (cache.get(code) as RosterCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterCycle> = inner.list()
}

fun newCachedRosterCycleStore(inner: RosterCycleStore, ttl: Int, clock: Clock): RosterCycleStore {
    return CachedRosterCycleStore(
        inner = inner,
        cache = newTtlCache<RosterCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster cycles. Empty fields match all. */
data class RosterCycleQuery(
    var category: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterCycle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterCycle>): MutableList<RosterCycle> {
        val matched = filter(items) { candidate: RosterCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterCycles(store: RosterCycleStore, query: RosterCycleQuery): MutableList<RosterCycle> {
    return query.apply(store.list())
}
