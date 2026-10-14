package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge groups by code. */
interface FallbackEvChargeGroupStore {
    fun find(code: String): FallbackEvChargeGroup?
    fun save(item: FallbackEvChargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeGroupStore(
    var items: MutableMap<String, FallbackEvChargeGroup>,
    var order: MutableList<String>,
) : FallbackEvChargeGroupStore {
    override fun find(code: String): FallbackEvChargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeGroup) {
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

    override fun list(): MutableList<FallbackEvChargeGroup> {
        val out = mutableListOf<FallbackEvChargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeGroupStore(): FallbackEvChargeGroupStore {
    return MemoryFallbackEvChargeGroupStore(
        mutableMapOf<String, FallbackEvChargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeGroupStore(
    var inner: FallbackEvChargeGroupStore,
    var cache: TtlCache<FallbackEvChargeGroup>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeGroupStore {
    override fun find(code: String): FallbackEvChargeGroup? {
        val hit = (cache.get(code) as FallbackEvChargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeGroup> = inner.list()
}

fun newCachedFallbackEvChargeGroupStore(inner: FallbackEvChargeGroupStore, ttl: Int, clock: Clock): FallbackEvChargeGroupStore {
    return CachedFallbackEvChargeGroupStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge groups. Empty fields match all. */
data class FallbackEvChargeGroupQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeGroup>): MutableList<FallbackEvChargeGroup> {
        val matched = filter(items) { candidate: FallbackEvChargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeGroups(store: FallbackEvChargeGroupStore, query: FallbackEvChargeGroupQuery): MutableList<FallbackEvChargeGroup> {
    return query.apply(store.list())
}
