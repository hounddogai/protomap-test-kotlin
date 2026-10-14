package com.acme.transport.customs.hold.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary hold overrides by code. */
interface PrimaryHoldOverrideStore {
    fun find(code: String): PrimaryHoldOverride?
    fun save(item: PrimaryHoldOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHoldOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHoldOverrideStore(
    var items: MutableMap<String, PrimaryHoldOverride>,
    var order: MutableList<String>,
) : PrimaryHoldOverrideStore {
    override fun find(code: String): PrimaryHoldOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHoldOverride) {
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

    override fun list(): MutableList<PrimaryHoldOverride> {
        val out = mutableListOf<PrimaryHoldOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHoldOverrideStore(): PrimaryHoldOverrideStore {
    return MemoryPrimaryHoldOverrideStore(mutableMapOf<String, PrimaryHoldOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHoldOverrideStore(
    var inner: PrimaryHoldOverrideStore,
    var cache: TtlCache<PrimaryHoldOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryHoldOverrideStore {
    override fun find(code: String): PrimaryHoldOverride? {
        val hit = (cache.get(code) as PrimaryHoldOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHoldOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHoldOverride> = inner.list()
}

fun newCachedPrimaryHoldOverrideStore(inner: PrimaryHoldOverrideStore, ttl: Int, clock: Clock): PrimaryHoldOverrideStore {
    return CachedPrimaryHoldOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryHoldOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hold overrides. Empty fields match all. */
data class PrimaryHoldOverrideQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHoldOverride): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHoldOverride>): MutableList<PrimaryHoldOverride> {
        val matched = filter(items) { candidate: PrimaryHoldOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHoldOverrides(store: PrimaryHoldOverrideStore, query: PrimaryHoldOverrideQuery): MutableList<PrimaryHoldOverride> {
    return query.apply(store.list())
}
