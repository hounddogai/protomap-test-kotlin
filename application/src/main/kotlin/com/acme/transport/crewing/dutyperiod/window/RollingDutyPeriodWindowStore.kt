package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling duty period windows by code. */
interface RollingDutyPeriodWindowStore {
    fun find(code: String): RollingDutyPeriodWindow?
    fun save(item: RollingDutyPeriodWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDutyPeriodWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDutyPeriodWindowStore(
    var items: MutableMap<String, RollingDutyPeriodWindow>,
    var order: MutableList<String>,
) : RollingDutyPeriodWindowStore {
    override fun find(code: String): RollingDutyPeriodWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDutyPeriodWindow) {
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

    override fun list(): MutableList<RollingDutyPeriodWindow> {
        val out = mutableListOf<RollingDutyPeriodWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDutyPeriodWindowStore(): RollingDutyPeriodWindowStore {
    return MemoryRollingDutyPeriodWindowStore(
        mutableMapOf<String, RollingDutyPeriodWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDutyPeriodWindowStore(
    var inner: RollingDutyPeriodWindowStore,
    var cache: TtlCache<RollingDutyPeriodWindow>,
    var hits: Int,
    var misses: Int,
) : RollingDutyPeriodWindowStore {
    override fun find(code: String): RollingDutyPeriodWindow? {
        val hit = (cache.get(code) as RollingDutyPeriodWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDutyPeriodWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDutyPeriodWindow> = inner.list()
}

fun newCachedRollingDutyPeriodWindowStore(inner: RollingDutyPeriodWindowStore, ttl: Int, clock: Clock): RollingDutyPeriodWindowStore {
    return CachedRollingDutyPeriodWindowStore(
        inner = inner,
        cache = newTtlCache<RollingDutyPeriodWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling duty period windows. Empty fields match all. */
data class RollingDutyPeriodWindowQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDutyPeriodWindow): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDutyPeriodWindow>): MutableList<RollingDutyPeriodWindow> {
        val matched = filter(items) { candidate: RollingDutyPeriodWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDutyPeriodWindows(store: RollingDutyPeriodWindowStore, query: RollingDutyPeriodWindowQuery): MutableList<RollingDutyPeriodWindow> {
    return query.apply(store.list())
}
