package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling carbon report overrides by code. */
interface RollingCarbonReportOverrideStore {
    fun find(code: String): RollingCarbonReportOverride?
    fun save(item: RollingCarbonReportOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCarbonReportOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCarbonReportOverrideStore(
    var items: MutableMap<String, RollingCarbonReportOverride>,
    var order: MutableList<String>,
) : RollingCarbonReportOverrideStore {
    override fun find(code: String): RollingCarbonReportOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCarbonReportOverride) {
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

    override fun list(): MutableList<RollingCarbonReportOverride> {
        val out = mutableListOf<RollingCarbonReportOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCarbonReportOverrideStore(): RollingCarbonReportOverrideStore {
    return MemoryRollingCarbonReportOverrideStore(
        mutableMapOf<String, RollingCarbonReportOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCarbonReportOverrideStore(
    var inner: RollingCarbonReportOverrideStore,
    var cache: TtlCache<RollingCarbonReportOverride>,
    var hits: Int,
    var misses: Int,
) : RollingCarbonReportOverrideStore {
    override fun find(code: String): RollingCarbonReportOverride? {
        val hit = (cache.get(code) as RollingCarbonReportOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCarbonReportOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCarbonReportOverride> = inner.list()
}

fun newCachedRollingCarbonReportOverrideStore(inner: RollingCarbonReportOverrideStore, ttl: Int, clock: Clock): RollingCarbonReportOverrideStore {
    return CachedRollingCarbonReportOverrideStore(
        inner = inner,
        cache = newTtlCache<RollingCarbonReportOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling carbon report overrides. Empty fields match all. */
data class RollingCarbonReportOverrideQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCarbonReportOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCarbonReportOverride>): MutableList<RollingCarbonReportOverride> {
        val matched = filter(items) { candidate: RollingCarbonReportOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCarbonReportOverrides(store: RollingCarbonReportOverrideStore, query: RollingCarbonReportOverrideQuery): MutableList<RollingCarbonReportOverride> {
    return query.apply(store.list())
}
