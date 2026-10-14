package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization tiers by code. */
interface UtilizationTierStore {
    fun find(code: String): UtilizationTier?
    fun save(item: UtilizationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationTierStore(
    var items: MutableMap<String, UtilizationTier>,
    var order: MutableList<String>,
) : UtilizationTierStore {
    override fun find(code: String): UtilizationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationTier) {
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

    override fun list(): MutableList<UtilizationTier> {
        val out = mutableListOf<UtilizationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationTierStore(): UtilizationTierStore {
    return MemoryUtilizationTierStore(mutableMapOf<String, UtilizationTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationTierStore(
    var inner: UtilizationTierStore,
    var cache: TtlCache<UtilizationTier>,
    var hits: Int,
    var misses: Int,
) : UtilizationTierStore {
    override fun find(code: String): UtilizationTier? {
        val hit = (cache.get(code) as UtilizationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationTier> = inner.list()
}

fun newCachedUtilizationTierStore(inner: UtilizationTierStore, ttl: Int, clock: Clock): UtilizationTierStore {
    return CachedUtilizationTierStore(
        inner = inner,
        cache = newTtlCache<UtilizationTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization tiers. Empty fields match all. */
data class UtilizationTierQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationTier>): MutableList<UtilizationTier> {
        val matched = filter(items) { candidate: UtilizationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationTiers(store: UtilizationTierStore, query: UtilizationTierQuery): MutableList<UtilizationTier> {
    return query.apply(store.list())
}
