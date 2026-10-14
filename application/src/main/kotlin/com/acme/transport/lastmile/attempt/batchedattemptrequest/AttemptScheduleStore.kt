package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt schedules by code. */
interface AttemptScheduleStore {
    fun find(code: String): AttemptSchedule?
    fun save(item: AttemptSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptScheduleStore(
    var items: MutableMap<String, AttemptSchedule>,
    var order: MutableList<String>,
) : AttemptScheduleStore {
    override fun find(code: String): AttemptSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptSchedule) {
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

    override fun list(): MutableList<AttemptSchedule> {
        val out = mutableListOf<AttemptSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptScheduleStore(): AttemptScheduleStore {
    return MemoryAttemptScheduleStore(mutableMapOf<String, AttemptSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptScheduleStore(
    var inner: AttemptScheduleStore,
    var cache: TtlCache<AttemptSchedule>,
    var hits: Int,
    var misses: Int,
) : AttemptScheduleStore {
    override fun find(code: String): AttemptSchedule? {
        val hit = (cache.get(code) as AttemptSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptSchedule> = inner.list()
}

fun newCachedAttemptScheduleStore(inner: AttemptScheduleStore, ttl: Int, clock: Clock): AttemptScheduleStore {
    return CachedAttemptScheduleStore(
        inner = inner,
        cache = newTtlCache<AttemptSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt schedules. Empty fields match all. */
data class AttemptScheduleQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptSchedule>): MutableList<AttemptSchedule> {
        val matched = filter(items) { candidate: AttemptSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptSchedules(store: AttemptScheduleStore, query: AttemptScheduleQuery): MutableList<AttemptSchedule> {
    return query.apply(store.list())
}
