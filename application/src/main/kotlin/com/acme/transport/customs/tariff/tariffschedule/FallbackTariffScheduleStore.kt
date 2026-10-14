package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback tariff schedules by code. */
interface FallbackTariffScheduleStore {
    fun find(code: String): FallbackTariffSchedule?
    fun save(item: FallbackTariffSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTariffSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTariffScheduleStore(
    var items: MutableMap<String, FallbackTariffSchedule>,
    var order: MutableList<String>,
) : FallbackTariffScheduleStore {
    override fun find(code: String): FallbackTariffSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTariffSchedule) {
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

    override fun list(): MutableList<FallbackTariffSchedule> {
        val out = mutableListOf<FallbackTariffSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTariffScheduleStore(): FallbackTariffScheduleStore {
    return MemoryFallbackTariffScheduleStore(
        mutableMapOf<String, FallbackTariffSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTariffScheduleStore(
    var inner: FallbackTariffScheduleStore,
    var cache: TtlCache<FallbackTariffSchedule>,
    var hits: Int,
    var misses: Int,
) : FallbackTariffScheduleStore {
    override fun find(code: String): FallbackTariffSchedule? {
        val hit = (cache.get(code) as FallbackTariffSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTariffSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTariffSchedule> = inner.list()
}

fun newCachedFallbackTariffScheduleStore(inner: FallbackTariffScheduleStore, ttl: Int, clock: Clock): FallbackTariffScheduleStore {
    return CachedFallbackTariffScheduleStore(
        inner = inner,
        cache = newTtlCache<FallbackTariffSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tariff schedules. Empty fields match all. */
data class FallbackTariffScheduleQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTariffSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackTariffSchedule>): MutableList<FallbackTariffSchedule> {
        val matched = filter(items) { candidate: FallbackTariffSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTariffSchedules(store: FallbackTariffScheduleStore, query: FallbackTariffScheduleQuery): MutableList<FallbackTariffSchedule> {
    return query.apply(store.list())
}
