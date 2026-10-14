package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event schedules by code. */
interface CameraEventScheduleStore {
    fun find(code: String): CameraEventSchedule?
    fun save(item: CameraEventSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventScheduleStore(
    var items: MutableMap<String, CameraEventSchedule>,
    var order: MutableList<String>,
) : CameraEventScheduleStore {
    override fun find(code: String): CameraEventSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventSchedule) {
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

    override fun list(): MutableList<CameraEventSchedule> {
        val out = mutableListOf<CameraEventSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventScheduleStore(): CameraEventScheduleStore {
    return MemoryCameraEventScheduleStore(mutableMapOf<String, CameraEventSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventScheduleStore(
    var inner: CameraEventScheduleStore,
    var cache: TtlCache<CameraEventSchedule>,
    var hits: Int,
    var misses: Int,
) : CameraEventScheduleStore {
    override fun find(code: String): CameraEventSchedule? {
        val hit = (cache.get(code) as CameraEventSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventSchedule> = inner.list()
}

fun newCachedCameraEventScheduleStore(inner: CameraEventScheduleStore, ttl: Int, clock: Clock): CameraEventScheduleStore {
    return CachedCameraEventScheduleStore(
        inner = inner,
        cache = newTtlCache<CameraEventSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event schedules. Empty fields match all. */
data class CameraEventScheduleQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventSchedule>): MutableList<CameraEventSchedule> {
        val matched = filter(items) { candidate: CameraEventSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventSchedules(store: CameraEventScheduleStore, query: CameraEventScheduleQuery): MutableList<CameraEventSchedule> {
    return query.apply(store.list())
}
