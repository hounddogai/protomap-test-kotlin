package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge plans by code. */
interface TieredEvChargePlanStore {
    fun find(code: String): TieredEvChargePlan?
    fun save(item: TieredEvChargePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargePlanStore(
    var items: MutableMap<String, TieredEvChargePlan>,
    var order: MutableList<String>,
) : TieredEvChargePlanStore {
    override fun find(code: String): TieredEvChargePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargePlan) {
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

    override fun list(): MutableList<TieredEvChargePlan> {
        val out = mutableListOf<TieredEvChargePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargePlanStore(): TieredEvChargePlanStore {
    return MemoryTieredEvChargePlanStore(mutableMapOf<String, TieredEvChargePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargePlanStore(
    var inner: TieredEvChargePlanStore,
    var cache: TtlCache<TieredEvChargePlan>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargePlanStore {
    override fun find(code: String): TieredEvChargePlan? {
        val hit = (cache.get(code) as TieredEvChargePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargePlan> = inner.list()
}

fun newCachedTieredEvChargePlanStore(inner: TieredEvChargePlanStore, ttl: Int, clock: Clock): TieredEvChargePlanStore {
    return CachedTieredEvChargePlanStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargePlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge plans. Empty fields match all. */
data class TieredEvChargePlanQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargePlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEvChargePlan>): MutableList<TieredEvChargePlan> {
        val matched = filter(items) { candidate: TieredEvChargePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargePlans(store: TieredEvChargePlanStore, query: TieredEvChargePlanQuery): MutableList<TieredEvChargePlan> {
    return query.apply(store.list())
}
