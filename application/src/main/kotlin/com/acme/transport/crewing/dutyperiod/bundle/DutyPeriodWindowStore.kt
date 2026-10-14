package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period windows by code. */
interface DutyPeriodWindowStore {
    fun find(code: String): DutyPeriodWindow?
    fun save(item: DutyPeriodWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodWindowStore(
    var items: MutableMap<String, DutyPeriodWindow>,
    var order: MutableList<String>,
) : DutyPeriodWindowStore {
    override fun find(code: String): DutyPeriodWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodWindow) {
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

    override fun list(): MutableList<DutyPeriodWindow> {
        val out = mutableListOf<DutyPeriodWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodWindowStore(): DutyPeriodWindowStore {
    return MemoryDutyPeriodWindowStore(mutableMapOf<String, DutyPeriodWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodWindowStore(
    var inner: DutyPeriodWindowStore,
    var cache: TtlCache<DutyPeriodWindow>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodWindowStore {
    override fun find(code: String): DutyPeriodWindow? {
        val hit = (cache.get(code) as DutyPeriodWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodWindow> = inner.list()
}

fun newCachedDutyPeriodWindowStore(inner: DutyPeriodWindowStore, ttl: Int, clock: Clock): DutyPeriodWindowStore {
    return CachedDutyPeriodWindowStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period windows. Empty fields match all. */
data class DutyPeriodWindowQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodWindow>): MutableList<DutyPeriodWindow> {
        val matched = filter(items) { candidate: DutyPeriodWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodWindows(store: DutyPeriodWindowStore, query: DutyPeriodWindowQuery): MutableList<DutyPeriodWindow> {
    return query.apply(store.list())
}
