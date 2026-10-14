package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tire plans by code. */
interface LegacyTirePlanStore {
    fun find(code: String): LegacyTirePlan?
    fun save(item: LegacyTirePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTirePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTirePlanStore(
    var items: MutableMap<String, LegacyTirePlan>,
    var order: MutableList<String>,
) : LegacyTirePlanStore {
    override fun find(code: String): LegacyTirePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTirePlan) {
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

    override fun list(): MutableList<LegacyTirePlan> {
        val out = mutableListOf<LegacyTirePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTirePlanStore(): LegacyTirePlanStore {
    return MemoryLegacyTirePlanStore(mutableMapOf<String, LegacyTirePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTirePlanStore(
    var inner: LegacyTirePlanStore,
    var cache: TtlCache<LegacyTirePlan>,
    var hits: Int,
    var misses: Int,
) : LegacyTirePlanStore {
    override fun find(code: String): LegacyTirePlan? {
        val hit = (cache.get(code) as LegacyTirePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTirePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTirePlan> = inner.list()
}

fun newCachedLegacyTirePlanStore(inner: LegacyTirePlanStore, ttl: Int, clock: Clock): LegacyTirePlanStore {
    return CachedLegacyTirePlanStore(
        inner = inner,
        cache = newTtlCache<LegacyTirePlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tire plans. Empty fields match all. */
data class LegacyTirePlanQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTirePlan): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<LegacyTirePlan>): MutableList<LegacyTirePlan> {
        val matched = filter(items) { candidate: LegacyTirePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTirePlans(store: LegacyTirePlanStore, query: LegacyTirePlanQuery): MutableList<LegacyTirePlan> {
    return query.apply(store.list())
}
