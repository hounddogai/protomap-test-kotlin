package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped cutoff schedules by code. */
interface CappedCutoffScheduleStore {
    fun find(code: String): CappedCutoffSchedule?
    fun save(item: CappedCutoffSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCutoffSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCutoffScheduleStore(
    var items: MutableMap<String, CappedCutoffSchedule>,
    var order: MutableList<String>,
) : CappedCutoffScheduleStore {
    override fun find(code: String): CappedCutoffSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCutoffSchedule) {
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

    override fun list(): MutableList<CappedCutoffSchedule> {
        val out = mutableListOf<CappedCutoffSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCutoffScheduleStore(): CappedCutoffScheduleStore {
    return MemoryCappedCutoffScheduleStore(
        mutableMapOf<String, CappedCutoffSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCutoffScheduleStore(
    var inner: CappedCutoffScheduleStore,
    var cache: TtlCache<CappedCutoffSchedule>,
    var hits: Int,
    var misses: Int,
) : CappedCutoffScheduleStore {
    override fun find(code: String): CappedCutoffSchedule? {
        val hit = (cache.get(code) as CappedCutoffSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCutoffSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCutoffSchedule> = inner.list()
}

fun newCachedCappedCutoffScheduleStore(inner: CappedCutoffScheduleStore, ttl: Int, clock: Clock): CappedCutoffScheduleStore {
    return CachedCappedCutoffScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedCutoffSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cutoff schedules. Empty fields match all. */
data class CappedCutoffScheduleQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCutoffSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedCutoffSchedule>): MutableList<CappedCutoffSchedule> {
        val matched = filter(items) { candidate: CappedCutoffSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCutoffSchedules(store: CappedCutoffScheduleStore, query: CappedCutoffScheduleQuery): MutableList<CappedCutoffSchedule> {
    return query.apply(store.list())
}
