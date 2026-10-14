package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending emission factor overrides by code. */
interface PendingEmissionFactorOverrideStore {
    fun find(code: String): PendingEmissionFactorOverride?
    fun save(item: PendingEmissionFactorOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEmissionFactorOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEmissionFactorOverrideStore(
    var items: MutableMap<String, PendingEmissionFactorOverride>,
    var order: MutableList<String>,
) : PendingEmissionFactorOverrideStore {
    override fun find(code: String): PendingEmissionFactorOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEmissionFactorOverride) {
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

    override fun list(): MutableList<PendingEmissionFactorOverride> {
        val out = mutableListOf<PendingEmissionFactorOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEmissionFactorOverrideStore(): PendingEmissionFactorOverrideStore {
    return MemoryPendingEmissionFactorOverrideStore(
        mutableMapOf<String, PendingEmissionFactorOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEmissionFactorOverrideStore(
    var inner: PendingEmissionFactorOverrideStore,
    var cache: TtlCache<PendingEmissionFactorOverride>,
    var hits: Int,
    var misses: Int,
) : PendingEmissionFactorOverrideStore {
    override fun find(code: String): PendingEmissionFactorOverride? {
        val hit = (cache.get(code) as PendingEmissionFactorOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEmissionFactorOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEmissionFactorOverride> = inner.list()
}

fun newCachedPendingEmissionFactorOverrideStore(inner: PendingEmissionFactorOverrideStore, ttl: Int, clock: Clock): PendingEmissionFactorOverrideStore {
    return CachedPendingEmissionFactorOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingEmissionFactorOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending emission factor overrides. Empty fields match all. */
data class PendingEmissionFactorOverrideQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEmissionFactorOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingEmissionFactorOverride>): MutableList<PendingEmissionFactorOverride> {
        val matched = filter(items) { candidate: PendingEmissionFactorOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEmissionFactorOverrides(store: PendingEmissionFactorOverrideStore, query: PendingEmissionFactorOverrideQuery): MutableList<PendingEmissionFactorOverride> {
    return query.apply(store.list())
}
