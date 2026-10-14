package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled duty period exceptions by code. */
interface ScheduledDutyPeriodExceptionStore {
    fun find(code: String): ScheduledDutyPeriodException?
    fun save(item: ScheduledDutyPeriodException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDutyPeriodException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDutyPeriodExceptionStore(
    var items: MutableMap<String, ScheduledDutyPeriodException>,
    var order: MutableList<String>,
) : ScheduledDutyPeriodExceptionStore {
    override fun find(code: String): ScheduledDutyPeriodException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDutyPeriodException) {
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

    override fun list(): MutableList<ScheduledDutyPeriodException> {
        val out = mutableListOf<ScheduledDutyPeriodException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDutyPeriodExceptionStore(): ScheduledDutyPeriodExceptionStore {
    return MemoryScheduledDutyPeriodExceptionStore(
        mutableMapOf<String, ScheduledDutyPeriodException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDutyPeriodExceptionStore(
    var inner: ScheduledDutyPeriodExceptionStore,
    var cache: TtlCache<ScheduledDutyPeriodException>,
    var hits: Int,
    var misses: Int,
) : ScheduledDutyPeriodExceptionStore {
    override fun find(code: String): ScheduledDutyPeriodException? {
        val hit = (cache.get(code) as ScheduledDutyPeriodException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDutyPeriodException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDutyPeriodException> = inner.list()
}

fun newCachedScheduledDutyPeriodExceptionStore(inner: ScheduledDutyPeriodExceptionStore, ttl: Int, clock: Clock): ScheduledDutyPeriodExceptionStore {
    return CachedScheduledDutyPeriodExceptionStore(
        inner = inner,
        cache = newTtlCache<ScheduledDutyPeriodException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled duty period exceptions. Empty fields match all. */
data class ScheduledDutyPeriodExceptionQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDutyPeriodException): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDutyPeriodException>): MutableList<ScheduledDutyPeriodException> {
        val matched = filter(items) { candidate: ScheduledDutyPeriodException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDutyPeriodExceptions(store: ScheduledDutyPeriodExceptionStore, query: ScheduledDutyPeriodExceptionQuery): MutableList<ScheduledDutyPeriodException> {
    return query.apply(store.list())
}
