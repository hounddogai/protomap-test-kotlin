package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict camera event tiers by code. */
interface StrictCameraEventTierStore {
    fun find(code: String): StrictCameraEventTier?
    fun save(item: StrictCameraEventTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCameraEventTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCameraEventTierStore(
    var items: MutableMap<String, StrictCameraEventTier>,
    var order: MutableList<String>,
) : StrictCameraEventTierStore {
    override fun find(code: String): StrictCameraEventTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCameraEventTier) {
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

    override fun list(): MutableList<StrictCameraEventTier> {
        val out = mutableListOf<StrictCameraEventTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCameraEventTierStore(): StrictCameraEventTierStore {
    return MemoryStrictCameraEventTierStore(
        mutableMapOf<String, StrictCameraEventTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCameraEventTierStore(
    var inner: StrictCameraEventTierStore,
    var cache: TtlCache<StrictCameraEventTier>,
    var hits: Int,
    var misses: Int,
) : StrictCameraEventTierStore {
    override fun find(code: String): StrictCameraEventTier? {
        val hit = (cache.get(code) as StrictCameraEventTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCameraEventTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCameraEventTier> = inner.list()
}

fun newCachedStrictCameraEventTierStore(inner: StrictCameraEventTierStore, ttl: Int, clock: Clock): StrictCameraEventTierStore {
    return CachedStrictCameraEventTierStore(
        inner = inner,
        cache = newTtlCache<StrictCameraEventTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict camera event tiers. Empty fields match all. */
data class StrictCameraEventTierQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCameraEventTier): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCameraEventTier>): MutableList<StrictCameraEventTier> {
        val matched = filter(items) { candidate: StrictCameraEventTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCameraEventTiers(store: StrictCameraEventTierStore, query: StrictCameraEventTierQuery): MutableList<StrictCameraEventTier> {
    return query.apply(store.list())
}
