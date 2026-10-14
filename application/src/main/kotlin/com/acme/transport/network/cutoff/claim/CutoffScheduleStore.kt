package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff schedules by code. */
interface CutoffScheduleStore {
    fun find(code: String): CutoffSchedule?
    fun save(item: CutoffSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffScheduleStore(
    var items: MutableMap<String, CutoffSchedule>,
    var order: MutableList<String>,
) : CutoffScheduleStore {
    override fun find(code: String): CutoffSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffSchedule) {
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

    override fun list(): MutableList<CutoffSchedule> {
        val out = mutableListOf<CutoffSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffScheduleStore(): CutoffScheduleStore {
    return MemoryCutoffScheduleStore(mutableMapOf<String, CutoffSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffScheduleStore(
    var inner: CutoffScheduleStore,
    var cache: TtlCache<CutoffSchedule>,
    var hits: Int,
    var misses: Int,
) : CutoffScheduleStore {
    override fun find(code: String): CutoffSchedule? {
        val hit = (cache.get(code) as CutoffSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffSchedule> = inner.list()
}

fun newCachedCutoffScheduleStore(inner: CutoffScheduleStore, ttl: Int, clock: Clock): CutoffScheduleStore {
    return CachedCutoffScheduleStore(
        inner = inner,
        cache = newTtlCache<CutoffSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff schedules. Empty fields match all. */
data class CutoffScheduleQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffSchedule>): MutableList<CutoffSchedule> {
        val matched = filter(items) { candidate: CutoffSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffSchedules(store: CutoffScheduleStore, query: CutoffScheduleQuery): MutableList<CutoffSchedule> {
    return query.apply(store.list())
}
