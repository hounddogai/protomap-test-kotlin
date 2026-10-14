package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending duty period exceptions by code. */
interface PendingDutyPeriodExceptionStore {
    fun find(code: String): PendingDutyPeriodException?
    fun save(item: PendingDutyPeriodException)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDutyPeriodException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDutyPeriodExceptionStore(
    var items: MutableMap<String, PendingDutyPeriodException>,
    var order: MutableList<String>,
) : PendingDutyPeriodExceptionStore {
    override fun find(code: String): PendingDutyPeriodException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDutyPeriodException) {
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

    override fun list(): MutableList<PendingDutyPeriodException> {
        val out = mutableListOf<PendingDutyPeriodException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDutyPeriodExceptionStore(): PendingDutyPeriodExceptionStore {
    return MemoryPendingDutyPeriodExceptionStore(
        mutableMapOf<String, PendingDutyPeriodException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDutyPeriodExceptionStore(
    var inner: PendingDutyPeriodExceptionStore,
    var cache: TtlCache<PendingDutyPeriodException>,
    var hits: Int,
    var misses: Int,
) : PendingDutyPeriodExceptionStore {
    override fun find(code: String): PendingDutyPeriodException? {
        val hit = (cache.get(code) as PendingDutyPeriodException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDutyPeriodException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDutyPeriodException> = inner.list()
}

fun newCachedPendingDutyPeriodExceptionStore(inner: PendingDutyPeriodExceptionStore, ttl: Int, clock: Clock): PendingDutyPeriodExceptionStore {
    return CachedPendingDutyPeriodExceptionStore(
        inner = inner,
        cache = newTtlCache<PendingDutyPeriodException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending duty period exceptions. Empty fields match all. */
data class PendingDutyPeriodExceptionQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDutyPeriodException): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<PendingDutyPeriodException>): MutableList<PendingDutyPeriodException> {
        val matched = filter(items) { candidate: PendingDutyPeriodException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDutyPeriodExceptions(store: PendingDutyPeriodExceptionStore, query: PendingDutyPeriodExceptionQuery): MutableList<PendingDutyPeriodException> {
    return query.apply(store.list())
}
