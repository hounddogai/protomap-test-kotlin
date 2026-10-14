package com.acme.transport.customs.tariff.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tariff schedules by code. */
interface ManualTariffScheduleStore {
    fun find(code: String): ManualTariffSchedule?
    fun save(item: ManualTariffSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTariffSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTariffScheduleStore(
    var items: MutableMap<String, ManualTariffSchedule>,
    var order: MutableList<String>,
) : ManualTariffScheduleStore {
    override fun find(code: String): ManualTariffSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTariffSchedule) {
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

    override fun list(): MutableList<ManualTariffSchedule> {
        val out = mutableListOf<ManualTariffSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTariffScheduleStore(): ManualTariffScheduleStore {
    return MemoryManualTariffScheduleStore(
        mutableMapOf<String, ManualTariffSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTariffScheduleStore(
    var inner: ManualTariffScheduleStore,
    var cache: TtlCache<ManualTariffSchedule>,
    var hits: Int,
    var misses: Int,
) : ManualTariffScheduleStore {
    override fun find(code: String): ManualTariffSchedule? {
        val hit = (cache.get(code) as ManualTariffSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTariffSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTariffSchedule> = inner.list()
}

fun newCachedManualTariffScheduleStore(inner: ManualTariffScheduleStore, ttl: Int, clock: Clock): ManualTariffScheduleStore {
    return CachedManualTariffScheduleStore(
        inner = inner,
        cache = newTtlCache<ManualTariffSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tariff schedules. Empty fields match all. */
data class ManualTariffScheduleQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTariffSchedule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTariffSchedule>): MutableList<ManualTariffSchedule> {
        val matched = filter(items) { candidate: ManualTariffSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTariffSchedules(store: ManualTariffScheduleStore, query: ManualTariffScheduleQuery): MutableList<ManualTariffSchedule> {
    return query.apply(store.list())
}
