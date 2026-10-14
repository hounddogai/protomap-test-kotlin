package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback service plan baselines by code. */
interface FallbackServicePlanBaselineStore {
    fun find(code: String): FallbackServicePlanBaseline?
    fun save(item: FallbackServicePlanBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServicePlanBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServicePlanBaselineStore(
    var items: MutableMap<String, FallbackServicePlanBaseline>,
    var order: MutableList<String>,
) : FallbackServicePlanBaselineStore {
    override fun find(code: String): FallbackServicePlanBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServicePlanBaseline) {
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

    override fun list(): MutableList<FallbackServicePlanBaseline> {
        val out = mutableListOf<FallbackServicePlanBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServicePlanBaselineStore(): FallbackServicePlanBaselineStore {
    return MemoryFallbackServicePlanBaselineStore(
        mutableMapOf<String, FallbackServicePlanBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServicePlanBaselineStore(
    var inner: FallbackServicePlanBaselineStore,
    var cache: TtlCache<FallbackServicePlanBaseline>,
    var hits: Int,
    var misses: Int,
) : FallbackServicePlanBaselineStore {
    override fun find(code: String): FallbackServicePlanBaseline? {
        val hit = (cache.get(code) as FallbackServicePlanBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServicePlanBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServicePlanBaseline> = inner.list()
}

fun newCachedFallbackServicePlanBaselineStore(inner: FallbackServicePlanBaselineStore, ttl: Int, clock: Clock): FallbackServicePlanBaselineStore {
    return CachedFallbackServicePlanBaselineStore(
        inner = inner,
        cache = newTtlCache<FallbackServicePlanBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service plan baselines. Empty fields match all. */
data class FallbackServicePlanBaselineQuery(
    var channel: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServicePlanBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<FallbackServicePlanBaseline>): MutableList<FallbackServicePlanBaseline> {
        val matched = filter(items) { candidate: FallbackServicePlanBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServicePlanBaselines(store: FallbackServicePlanBaselineStore, query: FallbackServicePlanBaselineQuery): MutableList<FallbackServicePlanBaseline> {
    return query.apply(store.list())
}
