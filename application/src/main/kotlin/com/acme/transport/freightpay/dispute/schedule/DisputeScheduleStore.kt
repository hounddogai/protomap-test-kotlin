package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute schedules by code. */
interface DisputeScheduleStore {
    fun find(code: String): DisputeSchedule?
    fun save(item: DisputeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeScheduleStore(
    var items: MutableMap<String, DisputeSchedule>,
    var order: MutableList<String>,
) : DisputeScheduleStore {
    override fun find(code: String): DisputeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeSchedule) {
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

    override fun list(): MutableList<DisputeSchedule> {
        val out = mutableListOf<DisputeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeScheduleStore(): DisputeScheduleStore {
    return MemoryDisputeScheduleStore(mutableMapOf<String, DisputeSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeScheduleStore(
    var inner: DisputeScheduleStore,
    var cache: TtlCache<DisputeSchedule>,
    var hits: Int,
    var misses: Int,
) : DisputeScheduleStore {
    override fun find(code: String): DisputeSchedule? {
        val hit = (cache.get(code) as DisputeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeSchedule> = inner.list()
}

fun newCachedDisputeScheduleStore(inner: DisputeScheduleStore, ttl: Int, clock: Clock): DisputeScheduleStore {
    return CachedDisputeScheduleStore(
        inner = inner,
        cache = newTtlCache<DisputeSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute schedules. Empty fields match all. */
data class DisputeScheduleQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeSchedule>): MutableList<DisputeSchedule> {
        val matched = filter(items) { candidate: DisputeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeSchedules(store: DisputeScheduleStore, query: DisputeScheduleQuery): MutableList<DisputeSchedule> {
    return query.apply(store.list())
}
