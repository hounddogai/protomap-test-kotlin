package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification schedules by code. */
interface QualificationScheduleStore {
    fun find(code: String): QualificationSchedule?
    fun save(item: QualificationSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationScheduleStore(
    var items: MutableMap<String, QualificationSchedule>,
    var order: MutableList<String>,
) : QualificationScheduleStore {
    override fun find(code: String): QualificationSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationSchedule) {
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

    override fun list(): MutableList<QualificationSchedule> {
        val out = mutableListOf<QualificationSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationScheduleStore(): QualificationScheduleStore {
    return MemoryQualificationScheduleStore(
        mutableMapOf<String, QualificationSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationScheduleStore(
    var inner: QualificationScheduleStore,
    var cache: TtlCache<QualificationSchedule>,
    var hits: Int,
    var misses: Int,
) : QualificationScheduleStore {
    override fun find(code: String): QualificationSchedule? {
        val hit = (cache.get(code) as QualificationSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationSchedule> = inner.list()
}

fun newCachedQualificationScheduleStore(inner: QualificationScheduleStore, ttl: Int, clock: Clock): QualificationScheduleStore {
    return CachedQualificationScheduleStore(
        inner = inner,
        cache = newTtlCache<QualificationSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification schedules. Empty fields match all. */
data class QualificationScheduleQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationSchedule>): MutableList<QualificationSchedule> {
        val matched = filter(items) { candidate: QualificationSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationSchedules(store: QualificationScheduleStore, query: QualificationScheduleQuery): MutableList<QualificationSchedule> {
    return query.apply(store.list())
}
