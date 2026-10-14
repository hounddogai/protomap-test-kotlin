package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict inspection policies by code. */
interface StrictInspectionPolicyStore {
    fun find(code: String): StrictInspectionPolicy?
    fun save(item: StrictInspectionPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictInspectionPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictInspectionPolicyStore(
    var items: MutableMap<String, StrictInspectionPolicy>,
    var order: MutableList<String>,
) : StrictInspectionPolicyStore {
    override fun find(code: String): StrictInspectionPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictInspectionPolicy) {
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

    override fun list(): MutableList<StrictInspectionPolicy> {
        val out = mutableListOf<StrictInspectionPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictInspectionPolicyStore(): StrictInspectionPolicyStore {
    return MemoryStrictInspectionPolicyStore(
        mutableMapOf<String, StrictInspectionPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictInspectionPolicyStore(
    var inner: StrictInspectionPolicyStore,
    var cache: TtlCache<StrictInspectionPolicy>,
    var hits: Int,
    var misses: Int,
) : StrictInspectionPolicyStore {
    override fun find(code: String): StrictInspectionPolicy? {
        val hit = (cache.get(code) as StrictInspectionPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictInspectionPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictInspectionPolicy> = inner.list()
}

fun newCachedStrictInspectionPolicyStore(inner: StrictInspectionPolicyStore, ttl: Int, clock: Clock): StrictInspectionPolicyStore {
    return CachedStrictInspectionPolicyStore(
        inner = inner,
        cache = newTtlCache<StrictInspectionPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict inspection policies. Empty fields match all. */
data class StrictInspectionPolicyQuery(
    var locale: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictInspectionPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<StrictInspectionPolicy>): MutableList<StrictInspectionPolicy> {
        val matched = filter(items) { candidate: StrictInspectionPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictInspectionPolicies(store: StrictInspectionPolicyStore, query: StrictInspectionPolicyQuery): MutableList<StrictInspectionPolicy> {
    return query.apply(store.list())
}
