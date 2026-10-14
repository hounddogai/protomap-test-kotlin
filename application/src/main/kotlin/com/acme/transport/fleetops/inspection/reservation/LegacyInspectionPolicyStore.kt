package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection policies by code. */
interface LegacyInspectionPolicyStore {
    fun find(code: String): LegacyInspectionPolicy?
    fun save(item: LegacyInspectionPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionPolicyStore(
    var items: MutableMap<String, LegacyInspectionPolicy>,
    var order: MutableList<String>,
) : LegacyInspectionPolicyStore {
    override fun find(code: String): LegacyInspectionPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionPolicy) {
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

    override fun list(): MutableList<LegacyInspectionPolicy> {
        val out = mutableListOf<LegacyInspectionPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionPolicyStore(): LegacyInspectionPolicyStore {
    return MemoryLegacyInspectionPolicyStore(
        mutableMapOf<String, LegacyInspectionPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionPolicyStore(
    var inner: LegacyInspectionPolicyStore,
    var cache: TtlCache<LegacyInspectionPolicy>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionPolicyStore {
    override fun find(code: String): LegacyInspectionPolicy? {
        val hit = (cache.get(code) as LegacyInspectionPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionPolicy> = inner.list()
}

fun newCachedLegacyInspectionPolicyStore(inner: LegacyInspectionPolicyStore, ttl: Int, clock: Clock): LegacyInspectionPolicyStore {
    return CachedLegacyInspectionPolicyStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection policies. Empty fields match all. */
data class LegacyInspectionPolicyQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<LegacyInspectionPolicy>): MutableList<LegacyInspectionPolicy> {
        val matched = filter(items) { candidate: LegacyInspectionPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionPolicies(store: LegacyInspectionPolicyStore, query: LegacyInspectionPolicyQuery): MutableList<LegacyInspectionPolicy> {
    return query.apply(store.list())
}
