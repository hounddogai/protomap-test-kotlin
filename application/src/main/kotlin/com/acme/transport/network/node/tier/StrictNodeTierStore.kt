package com.acme.transport.network.node.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict node tiers by code. */
interface StrictNodeTierStore {
    fun find(code: String): StrictNodeTier?
    fun save(item: StrictNodeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNodeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNodeTierStore(
    var items: MutableMap<String, StrictNodeTier>,
    var order: MutableList<String>,
) : StrictNodeTierStore {
    override fun find(code: String): StrictNodeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNodeTier) {
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

    override fun list(): MutableList<StrictNodeTier> {
        val out = mutableListOf<StrictNodeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNodeTierStore(): StrictNodeTierStore {
    return MemoryStrictNodeTierStore(mutableMapOf<String, StrictNodeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNodeTierStore(
    var inner: StrictNodeTierStore,
    var cache: TtlCache<StrictNodeTier>,
    var hits: Int,
    var misses: Int,
) : StrictNodeTierStore {
    override fun find(code: String): StrictNodeTier? {
        val hit = (cache.get(code) as StrictNodeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNodeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNodeTier> = inner.list()
}

fun newCachedStrictNodeTierStore(inner: StrictNodeTierStore, ttl: Int, clock: Clock): StrictNodeTierStore {
    return CachedStrictNodeTierStore(
        inner = inner,
        cache = newTtlCache<StrictNodeTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict node tiers. Empty fields match all. */
data class StrictNodeTierQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNodeTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictNodeTier>): MutableList<StrictNodeTier> {
        val matched = filter(items) { candidate: StrictNodeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNodeTiers(store: StrictNodeTierStore, query: StrictNodeTierQuery): MutableList<StrictNodeTier> {
    return query.apply(store.list())
}
