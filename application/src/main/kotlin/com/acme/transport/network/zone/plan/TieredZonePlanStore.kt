package com.acme.transport.network.zone.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered zone plans by code. */
interface TieredZonePlanStore {
    fun find(code: String): TieredZonePlan?
    fun save(item: TieredZonePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredZonePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredZonePlanStore(
    var items: MutableMap<String, TieredZonePlan>,
    var order: MutableList<String>,
) : TieredZonePlanStore {
    override fun find(code: String): TieredZonePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredZonePlan) {
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

    override fun list(): MutableList<TieredZonePlan> {
        val out = mutableListOf<TieredZonePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredZonePlanStore(): TieredZonePlanStore {
    return MemoryTieredZonePlanStore(mutableMapOf<String, TieredZonePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredZonePlanStore(
    var inner: TieredZonePlanStore,
    var cache: TtlCache<TieredZonePlan>,
    var hits: Int,
    var misses: Int,
) : TieredZonePlanStore {
    override fun find(code: String): TieredZonePlan? {
        val hit = (cache.get(code) as TieredZonePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredZonePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredZonePlan> = inner.list()
}

fun newCachedTieredZonePlanStore(inner: TieredZonePlanStore, ttl: Int, clock: Clock): TieredZonePlanStore {
    return CachedTieredZonePlanStore(
        inner = inner,
        cache = newTtlCache<TieredZonePlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered zone plans. Empty fields match all. */
data class TieredZonePlanQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredZonePlan): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredZonePlan>): MutableList<TieredZonePlan> {
        val matched = filter(items) { candidate: TieredZonePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredZonePlans(store: TieredZonePlanStore, query: TieredZonePlanQuery): MutableList<TieredZonePlan> {
    return query.apply(store.list())
}
