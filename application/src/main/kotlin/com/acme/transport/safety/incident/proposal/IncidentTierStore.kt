package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident tiers by code. */
interface IncidentTierStore {
    fun find(code: String): IncidentTier?
    fun save(item: IncidentTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentTierStore(
    var items: MutableMap<String, IncidentTier>,
    var order: MutableList<String>,
) : IncidentTierStore {
    override fun find(code: String): IncidentTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentTier) {
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

    override fun list(): MutableList<IncidentTier> {
        val out = mutableListOf<IncidentTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentTierStore(): IncidentTierStore {
    return MemoryIncidentTierStore(mutableMapOf<String, IncidentTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentTierStore(
    var inner: IncidentTierStore,
    var cache: TtlCache<IncidentTier>,
    var hits: Int,
    var misses: Int,
) : IncidentTierStore {
    override fun find(code: String): IncidentTier? {
        val hit = (cache.get(code) as IncidentTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentTier> = inner.list()
}

fun newCachedIncidentTierStore(inner: IncidentTierStore, ttl: Int, clock: Clock): IncidentTierStore {
    return CachedIncidentTierStore(
        inner = inner,
        cache = newTtlCache<IncidentTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident tiers. Empty fields match all. */
data class IncidentTierQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentTier): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<IncidentTier>): MutableList<IncidentTier> {
        val matched = filter(items) { candidate: IncidentTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentTiers(store: IncidentTierStore, query: IncidentTierQuery): MutableList<IncidentTier> {
    return query.apply(store.list())
}
