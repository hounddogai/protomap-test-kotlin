package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge policies by code. */
interface EvChargePolicyStore {
    fun find(code: String): EvChargePolicy?
    fun save(item: EvChargePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargePolicyStore(
    var items: MutableMap<String, EvChargePolicy>,
    var order: MutableList<String>,
) : EvChargePolicyStore {
    override fun find(code: String): EvChargePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargePolicy) {
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

    override fun list(): MutableList<EvChargePolicy> {
        val out = mutableListOf<EvChargePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargePolicyStore(): EvChargePolicyStore {
    return MemoryEvChargePolicyStore(mutableMapOf<String, EvChargePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargePolicyStore(
    var inner: EvChargePolicyStore,
    var cache: TtlCache<EvChargePolicy>,
    var hits: Int,
    var misses: Int,
) : EvChargePolicyStore {
    override fun find(code: String): EvChargePolicy? {
        val hit = (cache.get(code) as EvChargePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargePolicy> = inner.list()
}

fun newCachedEvChargePolicyStore(inner: EvChargePolicyStore, ttl: Int, clock: Clock): EvChargePolicyStore {
    return CachedEvChargePolicyStore(
        inner = inner,
        cache = newTtlCache<EvChargePolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge policies. Empty fields match all. */
data class EvChargePolicyQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargePolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargePolicy>): MutableList<EvChargePolicy> {
        val matched = filter(items) { candidate: EvChargePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargePolicies(store: EvChargePolicyStore, query: EvChargePolicyQuery): MutableList<EvChargePolicy> {
    return query.apply(store.list())
}
