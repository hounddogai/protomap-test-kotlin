package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global ev charge plans by code. */
interface GlobalEvChargePlanStore {
    fun find(code: String): GlobalEvChargePlan?
    fun save(item: GlobalEvChargePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalEvChargePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalEvChargePlanStore(
    var items: MutableMap<String, GlobalEvChargePlan>,
    var order: MutableList<String>,
) : GlobalEvChargePlanStore {
    override fun find(code: String): GlobalEvChargePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalEvChargePlan) {
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

    override fun list(): MutableList<GlobalEvChargePlan> {
        val out = mutableListOf<GlobalEvChargePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalEvChargePlanStore(): GlobalEvChargePlanStore {
    return MemoryGlobalEvChargePlanStore(mutableMapOf<String, GlobalEvChargePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalEvChargePlanStore(
    var inner: GlobalEvChargePlanStore,
    var cache: TtlCache<GlobalEvChargePlan>,
    var hits: Int,
    var misses: Int,
) : GlobalEvChargePlanStore {
    override fun find(code: String): GlobalEvChargePlan? {
        val hit = (cache.get(code) as GlobalEvChargePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalEvChargePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalEvChargePlan> = inner.list()
}

fun newCachedGlobalEvChargePlanStore(inner: GlobalEvChargePlanStore, ttl: Int, clock: Clock): GlobalEvChargePlanStore {
    return CachedGlobalEvChargePlanStore(
        inner = inner,
        cache = newTtlCache<GlobalEvChargePlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global ev charge plans. Empty fields match all. */
data class GlobalEvChargePlanQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalEvChargePlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalEvChargePlan>): MutableList<GlobalEvChargePlan> {
        val matched = filter(items) { candidate: GlobalEvChargePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalEvChargePlans(store: GlobalEvChargePlanStore, query: GlobalEvChargePlanQuery): MutableList<GlobalEvChargePlan> {
    return query.apply(store.list())
}
