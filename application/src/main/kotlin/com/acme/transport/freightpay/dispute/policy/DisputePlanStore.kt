package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute plans by code. */
interface DisputePlanStore {
    fun find(code: String): DisputePlan?
    fun save(item: DisputePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputePlanStore(
    var items: MutableMap<String, DisputePlan>,
    var order: MutableList<String>,
) : DisputePlanStore {
    override fun find(code: String): DisputePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputePlan) {
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

    override fun list(): MutableList<DisputePlan> {
        val out = mutableListOf<DisputePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputePlanStore(): DisputePlanStore {
    return MemoryDisputePlanStore(mutableMapOf<String, DisputePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputePlanStore(
    var inner: DisputePlanStore,
    var cache: TtlCache<DisputePlan>,
    var hits: Int,
    var misses: Int,
) : DisputePlanStore {
    override fun find(code: String): DisputePlan? {
        val hit = (cache.get(code) as DisputePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputePlan> = inner.list()
}

fun newCachedDisputePlanStore(inner: DisputePlanStore, ttl: Int, clock: Clock): DisputePlanStore {
    return CachedDisputePlanStore(
        inner = inner,
        cache = newTtlCache<DisputePlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute plans. Empty fields match all. */
data class DisputePlanQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputePlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputePlan>): MutableList<DisputePlan> {
        val matched = filter(items) { candidate: DisputePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputePlans(store: DisputePlanStore, query: DisputePlanQuery): MutableList<DisputePlan> {
    return query.apply(store.list())
}
