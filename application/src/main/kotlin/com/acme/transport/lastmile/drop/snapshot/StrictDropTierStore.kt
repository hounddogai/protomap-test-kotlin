package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict drop tiers by code. */
interface StrictDropTierStore {
    fun find(code: String): StrictDropTier?
    fun save(item: StrictDropTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDropTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDropTierStore(
    var items: MutableMap<String, StrictDropTier>,
    var order: MutableList<String>,
) : StrictDropTierStore {
    override fun find(code: String): StrictDropTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDropTier) {
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

    override fun list(): MutableList<StrictDropTier> {
        val out = mutableListOf<StrictDropTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDropTierStore(): StrictDropTierStore {
    return MemoryStrictDropTierStore(mutableMapOf<String, StrictDropTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDropTierStore(
    var inner: StrictDropTierStore,
    var cache: TtlCache<StrictDropTier>,
    var hits: Int,
    var misses: Int,
) : StrictDropTierStore {
    override fun find(code: String): StrictDropTier? {
        val hit = (cache.get(code) as StrictDropTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDropTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDropTier> = inner.list()
}

fun newCachedStrictDropTierStore(inner: StrictDropTierStore, ttl: Int, clock: Clock): StrictDropTierStore {
    return CachedStrictDropTierStore(
        inner = inner,
        cache = newTtlCache<StrictDropTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict drop tiers. Empty fields match all. */
data class StrictDropTierQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDropTier): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDropTier>): MutableList<StrictDropTier> {
        val matched = filter(items) { candidate: StrictDropTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDropTiers(store: StrictDropTierStore, query: StrictDropTierQuery): MutableList<StrictDropTier> {
    return query.apply(store.list())
}
