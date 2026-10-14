package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial schedules by code. */
interface AccessorialScheduleStore {
    fun find(code: String): AccessorialSchedule?
    fun save(item: AccessorialSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialScheduleStore(
    var items: MutableMap<String, AccessorialSchedule>,
    var order: MutableList<String>,
) : AccessorialScheduleStore {
    override fun find(code: String): AccessorialSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialSchedule) {
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

    override fun list(): MutableList<AccessorialSchedule> {
        val out = mutableListOf<AccessorialSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialScheduleStore(): AccessorialScheduleStore {
    return MemoryAccessorialScheduleStore(mutableMapOf<String, AccessorialSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialScheduleStore(
    var inner: AccessorialScheduleStore,
    var cache: TtlCache<AccessorialSchedule>,
    var hits: Int,
    var misses: Int,
) : AccessorialScheduleStore {
    override fun find(code: String): AccessorialSchedule? {
        val hit = (cache.get(code) as AccessorialSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialSchedule> = inner.list()
}

fun newCachedAccessorialScheduleStore(inner: AccessorialScheduleStore, ttl: Int, clock: Clock): AccessorialScheduleStore {
    return CachedAccessorialScheduleStore(
        inner = inner,
        cache = newTtlCache<AccessorialSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial schedules. Empty fields match all. */
data class AccessorialScheduleQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialSchedule>): MutableList<AccessorialSchedule> {
        val matched = filter(items) { candidate: AccessorialSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialSchedules(store: AccessorialScheduleStore, query: AccessorialScheduleQuery): MutableList<AccessorialSchedule> {
    return query.apply(store.list())
}
