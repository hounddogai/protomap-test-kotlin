package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance policies by code. */
interface ClearancePolicyStore {
    fun find(code: String): ClearancePolicy?
    fun save(item: ClearancePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearancePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearancePolicyStore(
    var items: MutableMap<String, ClearancePolicy>,
    var order: MutableList<String>,
) : ClearancePolicyStore {
    override fun find(code: String): ClearancePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearancePolicy) {
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

    override fun list(): MutableList<ClearancePolicy> {
        val out = mutableListOf<ClearancePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearancePolicyStore(): ClearancePolicyStore {
    return MemoryClearancePolicyStore(mutableMapOf<String, ClearancePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearancePolicyStore(
    var inner: ClearancePolicyStore,
    var cache: TtlCache<ClearancePolicy>,
    var hits: Int,
    var misses: Int,
) : ClearancePolicyStore {
    override fun find(code: String): ClearancePolicy? {
        val hit = (cache.get(code) as ClearancePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearancePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearancePolicy> = inner.list()
}

fun newCachedClearancePolicyStore(inner: ClearancePolicyStore, ttl: Int, clock: Clock): ClearancePolicyStore {
    return CachedClearancePolicyStore(
        inner = inner,
        cache = newTtlCache<ClearancePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance policies. Empty fields match all. */
data class ClearancePolicyQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearancePolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearancePolicy>): MutableList<ClearancePolicy> {
        val matched = filter(items) { candidate: ClearancePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearancePolicies(store: ClearancePolicyStore, query: ClearancePolicyQuery): MutableList<ClearancePolicy> {
    return query.apply(store.list())
}
