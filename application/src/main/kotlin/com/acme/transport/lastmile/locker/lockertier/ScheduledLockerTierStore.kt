package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker tiers by code. */
interface ScheduledLockerTierStore {
    fun find(code: String): ScheduledLockerTier?
    fun save(item: ScheduledLockerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerTierStore(
    var items: MutableMap<String, ScheduledLockerTier>,
    var order: MutableList<String>,
) : ScheduledLockerTierStore {
    override fun find(code: String): ScheduledLockerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerTier) {
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

    override fun list(): MutableList<ScheduledLockerTier> {
        val out = mutableListOf<ScheduledLockerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerTierStore(): ScheduledLockerTierStore {
    return MemoryScheduledLockerTierStore(mutableMapOf<String, ScheduledLockerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerTierStore(
    var inner: ScheduledLockerTierStore,
    var cache: TtlCache<ScheduledLockerTier>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerTierStore {
    override fun find(code: String): ScheduledLockerTier? {
        val hit = (cache.get(code) as ScheduledLockerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerTier> = inner.list()
}

fun newCachedScheduledLockerTierStore(inner: ScheduledLockerTierStore, ttl: Int, clock: Clock): ScheduledLockerTierStore {
    return CachedScheduledLockerTierStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker tiers. Empty fields match all. */
data class ScheduledLockerTierQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerTier): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLockerTier>): MutableList<ScheduledLockerTier> {
        val matched = filter(items) { candidate: ScheduledLockerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerTiers(store: ScheduledLockerTierStore, query: ScheduledLockerTierQuery): MutableList<ScheduledLockerTier> {
    return query.apply(store.list())
}
