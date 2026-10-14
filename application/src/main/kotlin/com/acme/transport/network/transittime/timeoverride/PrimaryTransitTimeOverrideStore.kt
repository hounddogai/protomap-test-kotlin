package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time overrides by code. */
interface PrimaryTransitTimeOverrideStore {
    fun find(code: String): PrimaryTransitTimeOverride?
    fun save(item: PrimaryTransitTimeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeOverrideStore(
    var items: MutableMap<String, PrimaryTransitTimeOverride>,
    var order: MutableList<String>,
) : PrimaryTransitTimeOverrideStore {
    override fun find(code: String): PrimaryTransitTimeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeOverride) {
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

    override fun list(): MutableList<PrimaryTransitTimeOverride> {
        val out = mutableListOf<PrimaryTransitTimeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeOverrideStore(): PrimaryTransitTimeOverrideStore {
    return MemoryPrimaryTransitTimeOverrideStore(
        mutableMapOf<String, PrimaryTransitTimeOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeOverrideStore(
    var inner: PrimaryTransitTimeOverrideStore,
    var cache: TtlCache<PrimaryTransitTimeOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeOverrideStore {
    override fun find(code: String): PrimaryTransitTimeOverride? {
        val hit = (cache.get(code) as PrimaryTransitTimeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeOverride> = inner.list()
}

fun newCachedPrimaryTransitTimeOverrideStore(inner: PrimaryTransitTimeOverrideStore, ttl: Int, clock: Clock): PrimaryTransitTimeOverrideStore {
    return CachedPrimaryTransitTimeOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time overrides. Empty fields match all. */
data class PrimaryTransitTimeOverrideQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryTransitTimeOverride>): MutableList<PrimaryTransitTimeOverride> {
        val matched = filter(items) { candidate: PrimaryTransitTimeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeOverrides(store: PrimaryTransitTimeOverrideStore, query: PrimaryTransitTimeOverrideQuery): MutableList<PrimaryTransitTimeOverride> {
    return query.apply(store.list())
}
