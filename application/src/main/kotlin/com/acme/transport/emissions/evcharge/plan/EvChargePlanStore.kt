package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge plans by code. */
interface EvChargePlanStore {
    fun find(code: String): EvChargePlan?
    fun save(item: EvChargePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargePlanStore(
    var items: MutableMap<String, EvChargePlan>,
    var order: MutableList<String>,
) : EvChargePlanStore {
    override fun find(code: String): EvChargePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargePlan) {
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

    override fun list(): MutableList<EvChargePlan> {
        val out = mutableListOf<EvChargePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargePlanStore(): EvChargePlanStore {
    return MemoryEvChargePlanStore(mutableMapOf<String, EvChargePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargePlanStore(
    var inner: EvChargePlanStore,
    var cache: TtlCache<EvChargePlan>,
    var hits: Int,
    var misses: Int,
) : EvChargePlanStore {
    override fun find(code: String): EvChargePlan? {
        val hit = (cache.get(code) as EvChargePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargePlan> = inner.list()
}

fun newCachedEvChargePlanStore(inner: EvChargePlanStore, ttl: Int, clock: Clock): EvChargePlanStore {
    return CachedEvChargePlanStore(
        inner = inner,
        cache = newTtlCache<EvChargePlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge plans. Empty fields match all. */
data class EvChargePlanQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargePlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargePlan>): MutableList<EvChargePlan> {
        val matched = filter(items) { candidate: EvChargePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargePlans(store: EvChargePlanStore, query: EvChargePlanQuery): MutableList<EvChargePlan> {
    return query.apply(store.list())
}
