package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker tiers by code. */
interface LockerTierStore {
    fun find(code: String): LockerTier?
    fun save(item: LockerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerTierStore(
    var items: MutableMap<String, LockerTier>,
    var order: MutableList<String>,
) : LockerTierStore {
    override fun find(code: String): LockerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerTier) {
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

    override fun list(): MutableList<LockerTier> {
        val out = mutableListOf<LockerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerTierStore(): LockerTierStore {
    return MemoryLockerTierStore(mutableMapOf<String, LockerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerTierStore(
    var inner: LockerTierStore,
    var cache: TtlCache<LockerTier>,
    var hits: Int,
    var misses: Int,
) : LockerTierStore {
    override fun find(code: String): LockerTier? {
        val hit = (cache.get(code) as LockerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerTier> = inner.list()
}

fun newCachedLockerTierStore(inner: LockerTierStore, ttl: Int, clock: Clock): LockerTierStore {
    return CachedLockerTierStore(
        inner = inner,
        cache = newTtlCache<LockerTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker tiers. Empty fields match all. */
data class LockerTierQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LockerTier>): MutableList<LockerTier> {
        val matched = filter(items) { candidate: LockerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerTiers(store: LockerTierStore, query: LockerTierQuery): MutableList<LockerTier> {
    return query.apply(store.list())
}
