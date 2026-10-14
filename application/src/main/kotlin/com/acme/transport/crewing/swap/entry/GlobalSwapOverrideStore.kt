package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global swap overrides by code. */
interface GlobalSwapOverrideStore {
    fun find(code: String): GlobalSwapOverride?
    fun save(item: GlobalSwapOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSwapOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSwapOverrideStore(
    var items: MutableMap<String, GlobalSwapOverride>,
    var order: MutableList<String>,
) : GlobalSwapOverrideStore {
    override fun find(code: String): GlobalSwapOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSwapOverride) {
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

    override fun list(): MutableList<GlobalSwapOverride> {
        val out = mutableListOf<GlobalSwapOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSwapOverrideStore(): GlobalSwapOverrideStore {
    return MemoryGlobalSwapOverrideStore(mutableMapOf<String, GlobalSwapOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSwapOverrideStore(
    var inner: GlobalSwapOverrideStore,
    var cache: TtlCache<GlobalSwapOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalSwapOverrideStore {
    override fun find(code: String): GlobalSwapOverride? {
        val hit = (cache.get(code) as GlobalSwapOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSwapOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSwapOverride> = inner.list()
}

fun newCachedGlobalSwapOverrideStore(inner: GlobalSwapOverrideStore, ttl: Int, clock: Clock): GlobalSwapOverrideStore {
    return CachedGlobalSwapOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalSwapOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global swap overrides. Empty fields match all. */
data class GlobalSwapOverrideQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSwapOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSwapOverride>): MutableList<GlobalSwapOverride> {
        val matched = filter(items) { candidate: GlobalSwapOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSwapOverrides(store: GlobalSwapOverrideStore, query: GlobalSwapOverrideQuery): MutableList<GlobalSwapOverride> {
    return query.apply(store.list())
}
