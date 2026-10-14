package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict dispute plans by code. */
interface StrictDisputePlanStore {
    fun find(code: String): StrictDisputePlan?
    fun save(item: StrictDisputePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDisputePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDisputePlanStore(
    var items: MutableMap<String, StrictDisputePlan>,
    var order: MutableList<String>,
) : StrictDisputePlanStore {
    override fun find(code: String): StrictDisputePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDisputePlan) {
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

    override fun list(): MutableList<StrictDisputePlan> {
        val out = mutableListOf<StrictDisputePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDisputePlanStore(): StrictDisputePlanStore {
    return MemoryStrictDisputePlanStore(mutableMapOf<String, StrictDisputePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDisputePlanStore(
    var inner: StrictDisputePlanStore,
    var cache: TtlCache<StrictDisputePlan>,
    var hits: Int,
    var misses: Int,
) : StrictDisputePlanStore {
    override fun find(code: String): StrictDisputePlan? {
        val hit = (cache.get(code) as StrictDisputePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDisputePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDisputePlan> = inner.list()
}

fun newCachedStrictDisputePlanStore(inner: StrictDisputePlanStore, ttl: Int, clock: Clock): StrictDisputePlanStore {
    return CachedStrictDisputePlanStore(
        inner = inner,
        cache = newTtlCache<StrictDisputePlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dispute plans. Empty fields match all. */
data class StrictDisputePlanQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDisputePlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDisputePlan>): MutableList<StrictDisputePlan> {
        val matched = filter(items) { candidate: StrictDisputePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDisputePlans(store: StrictDisputePlanStore, query: StrictDisputePlanQuery): MutableList<StrictDisputePlan> {
    return query.apply(store.list())
}
