package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report policies by code. */
interface CarbonReportPolicyStore {
    fun find(code: String): CarbonReportPolicy?
    fun save(item: CarbonReportPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportPolicyStore(
    var items: MutableMap<String, CarbonReportPolicy>,
    var order: MutableList<String>,
) : CarbonReportPolicyStore {
    override fun find(code: String): CarbonReportPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportPolicy) {
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

    override fun list(): MutableList<CarbonReportPolicy> {
        val out = mutableListOf<CarbonReportPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportPolicyStore(): CarbonReportPolicyStore {
    return MemoryCarbonReportPolicyStore(mutableMapOf<String, CarbonReportPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportPolicyStore(
    var inner: CarbonReportPolicyStore,
    var cache: TtlCache<CarbonReportPolicy>,
    var hits: Int,
    var misses: Int,
) : CarbonReportPolicyStore {
    override fun find(code: String): CarbonReportPolicy? {
        val hit = (cache.get(code) as CarbonReportPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportPolicy> = inner.list()
}

fun newCachedCarbonReportPolicyStore(inner: CarbonReportPolicyStore, ttl: Int, clock: Clock): CarbonReportPolicyStore {
    return CachedCarbonReportPolicyStore(
        inner = inner,
        cache = newTtlCache<CarbonReportPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report policies. Empty fields match all. */
data class CarbonReportPolicyQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportPolicy>): MutableList<CarbonReportPolicy> {
        val matched = filter(items) { candidate: CarbonReportPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportPolicies(store: CarbonReportPolicyStore, query: CarbonReportPolicyQuery): MutableList<CarbonReportPolicy> {
    return query.apply(store.list())
}
