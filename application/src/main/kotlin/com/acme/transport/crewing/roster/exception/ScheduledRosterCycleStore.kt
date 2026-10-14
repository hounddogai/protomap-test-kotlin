package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster cycles by code. */
interface ScheduledRosterCycleStore {
    fun find(code: String): ScheduledRosterCycle?
    fun save(item: ScheduledRosterCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterCycleStore(
    var items: MutableMap<String, ScheduledRosterCycle>,
    var order: MutableList<String>,
) : ScheduledRosterCycleStore {
    override fun find(code: String): ScheduledRosterCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterCycle) {
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

    override fun list(): MutableList<ScheduledRosterCycle> {
        val out = mutableListOf<ScheduledRosterCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterCycleStore(): ScheduledRosterCycleStore {
    return MemoryScheduledRosterCycleStore(
        mutableMapOf<String, ScheduledRosterCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterCycleStore(
    var inner: ScheduledRosterCycleStore,
    var cache: TtlCache<ScheduledRosterCycle>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterCycleStore {
    override fun find(code: String): ScheduledRosterCycle? {
        val hit = (cache.get(code) as ScheduledRosterCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterCycle> = inner.list()
}

fun newCachedScheduledRosterCycleStore(inner: ScheduledRosterCycleStore, ttl: Int, clock: Clock): ScheduledRosterCycleStore {
    return CachedScheduledRosterCycleStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster cycles. Empty fields match all. */
data class ScheduledRosterCycleQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRosterCycle>): MutableList<ScheduledRosterCycle> {
        val matched = filter(items) { candidate: ScheduledRosterCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterCycles(store: ScheduledRosterCycleStore, query: ScheduledRosterCycleQuery): MutableList<ScheduledRosterCycle> {
    return query.apply(store.list())
}
