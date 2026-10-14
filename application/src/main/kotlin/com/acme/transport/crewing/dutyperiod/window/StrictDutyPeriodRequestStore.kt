package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict duty period requests by code. */
interface StrictDutyPeriodRequestStore {
    fun find(code: String): StrictDutyPeriodRequest?
    fun save(item: StrictDutyPeriodRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDutyPeriodRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDutyPeriodRequestStore(
    var items: MutableMap<String, StrictDutyPeriodRequest>,
    var order: MutableList<String>,
) : StrictDutyPeriodRequestStore {
    override fun find(code: String): StrictDutyPeriodRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDutyPeriodRequest) {
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

    override fun list(): MutableList<StrictDutyPeriodRequest> {
        val out = mutableListOf<StrictDutyPeriodRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDutyPeriodRequestStore(): StrictDutyPeriodRequestStore {
    return MemoryStrictDutyPeriodRequestStore(
        mutableMapOf<String, StrictDutyPeriodRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDutyPeriodRequestStore(
    var inner: StrictDutyPeriodRequestStore,
    var cache: TtlCache<StrictDutyPeriodRequest>,
    var hits: Int,
    var misses: Int,
) : StrictDutyPeriodRequestStore {
    override fun find(code: String): StrictDutyPeriodRequest? {
        val hit = (cache.get(code) as StrictDutyPeriodRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDutyPeriodRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDutyPeriodRequest> = inner.list()
}

fun newCachedStrictDutyPeriodRequestStore(inner: StrictDutyPeriodRequestStore, ttl: Int, clock: Clock): StrictDutyPeriodRequestStore {
    return CachedStrictDutyPeriodRequestStore(
        inner = inner,
        cache = newTtlCache<StrictDutyPeriodRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict duty period requests. Empty fields match all. */
data class StrictDutyPeriodRequestQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDutyPeriodRequest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDutyPeriodRequest>): MutableList<StrictDutyPeriodRequest> {
        val matched = filter(items) { candidate: StrictDutyPeriodRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDutyPeriodRequests(store: StrictDutyPeriodRequestStore, query: StrictDutyPeriodRequestQuery): MutableList<StrictDutyPeriodRequest> {
    return query.apply(store.list())
}
