package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary carbon report policies by code. */
interface PrimaryCarbonReportPolicyStore {
    fun find(code: String): PrimaryCarbonReportPolicy?
    fun save(item: PrimaryCarbonReportPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCarbonReportPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCarbonReportPolicyStore(
    var items: MutableMap<String, PrimaryCarbonReportPolicy>,
    var order: MutableList<String>,
) : PrimaryCarbonReportPolicyStore {
    override fun find(code: String): PrimaryCarbonReportPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCarbonReportPolicy) {
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

    override fun list(): MutableList<PrimaryCarbonReportPolicy> {
        val out = mutableListOf<PrimaryCarbonReportPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCarbonReportPolicyStore(): PrimaryCarbonReportPolicyStore {
    return MemoryPrimaryCarbonReportPolicyStore(
        mutableMapOf<String, PrimaryCarbonReportPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCarbonReportPolicyStore(
    var inner: PrimaryCarbonReportPolicyStore,
    var cache: TtlCache<PrimaryCarbonReportPolicy>,
    var hits: Int,
    var misses: Int,
) : PrimaryCarbonReportPolicyStore {
    override fun find(code: String): PrimaryCarbonReportPolicy? {
        val hit = (cache.get(code) as PrimaryCarbonReportPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCarbonReportPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCarbonReportPolicy> = inner.list()
}

fun newCachedPrimaryCarbonReportPolicyStore(inner: PrimaryCarbonReportPolicyStore, ttl: Int, clock: Clock): PrimaryCarbonReportPolicyStore {
    return CachedPrimaryCarbonReportPolicyStore(
        inner = inner,
        cache = newTtlCache<PrimaryCarbonReportPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary carbon report policies. Empty fields match all. */
data class PrimaryCarbonReportPolicyQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCarbonReportPolicy): Boolean {
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

    fun apply(items: MutableList<PrimaryCarbonReportPolicy>): MutableList<PrimaryCarbonReportPolicy> {
        val matched = filter(items) { candidate: PrimaryCarbonReportPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCarbonReportPolicies(store: PrimaryCarbonReportPolicyStore, query: PrimaryCarbonReportPolicyQuery): MutableList<PrimaryCarbonReportPolicy> {
    return query.apply(store.list())
}
