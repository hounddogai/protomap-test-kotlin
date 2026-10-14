package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient availability tiers by code. */
interface LenientAvailabilityTierStore {
    fun find(code: String): LenientAvailabilityTier?
    fun save(item: LenientAvailabilityTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAvailabilityTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAvailabilityTierStore(
    var items: MutableMap<String, LenientAvailabilityTier>,
    var order: MutableList<String>,
) : LenientAvailabilityTierStore {
    override fun find(code: String): LenientAvailabilityTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAvailabilityTier) {
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

    override fun list(): MutableList<LenientAvailabilityTier> {
        val out = mutableListOf<LenientAvailabilityTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAvailabilityTierStore(): LenientAvailabilityTierStore {
    return MemoryLenientAvailabilityTierStore(
        mutableMapOf<String, LenientAvailabilityTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAvailabilityTierStore(
    var inner: LenientAvailabilityTierStore,
    var cache: TtlCache<LenientAvailabilityTier>,
    var hits: Int,
    var misses: Int,
) : LenientAvailabilityTierStore {
    override fun find(code: String): LenientAvailabilityTier? {
        val hit = (cache.get(code) as LenientAvailabilityTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAvailabilityTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAvailabilityTier> = inner.list()
}

fun newCachedLenientAvailabilityTierStore(inner: LenientAvailabilityTierStore, ttl: Int, clock: Clock): LenientAvailabilityTierStore {
    return CachedLenientAvailabilityTierStore(
        inner = inner,
        cache = newTtlCache<LenientAvailabilityTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient availability tiers. Empty fields match all. */
data class LenientAvailabilityTierQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAvailabilityTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAvailabilityTier>): MutableList<LenientAvailabilityTier> {
        val matched = filter(items) { candidate: LenientAvailabilityTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAvailabilityTiers(store: LenientAvailabilityTierStore, query: LenientAvailabilityTierQuery): MutableList<LenientAvailabilityTier> {
    return query.apply(store.list())
}
