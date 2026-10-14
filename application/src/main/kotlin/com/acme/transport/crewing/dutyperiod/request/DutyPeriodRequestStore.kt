package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period requests by code. */
interface DutyPeriodRequestStore {
    fun find(code: String): DutyPeriodRequest?
    fun save(item: DutyPeriodRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodRequestStore(
    var items: MutableMap<String, DutyPeriodRequest>,
    var order: MutableList<String>,
) : DutyPeriodRequestStore {
    override fun find(code: String): DutyPeriodRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodRequest) {
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

    override fun list(): MutableList<DutyPeriodRequest> {
        val out = mutableListOf<DutyPeriodRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodRequestStore(): DutyPeriodRequestStore {
    return MemoryDutyPeriodRequestStore(mutableMapOf<String, DutyPeriodRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodRequestStore(
    var inner: DutyPeriodRequestStore,
    var cache: TtlCache<DutyPeriodRequest>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodRequestStore {
    override fun find(code: String): DutyPeriodRequest? {
        val hit = (cache.get(code) as DutyPeriodRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodRequest> = inner.list()
}

fun newCachedDutyPeriodRequestStore(inner: DutyPeriodRequestStore, ttl: Int, clock: Clock): DutyPeriodRequestStore {
    return CachedDutyPeriodRequestStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period requests. Empty fields match all. */
data class DutyPeriodRequestQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodRequest>): MutableList<DutyPeriodRequest> {
        val matched = filter(items) { candidate: DutyPeriodRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodRequests(store: DutyPeriodRequestStore, query: DutyPeriodRequestQuery): MutableList<DutyPeriodRequest> {
    return query.apply(store.list())
}
