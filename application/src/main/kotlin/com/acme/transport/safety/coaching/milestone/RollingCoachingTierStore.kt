package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching tiers by code. */
interface RollingCoachingTierStore {
    fun find(code: String): RollingCoachingTier?
    fun save(item: RollingCoachingTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingTierStore(
    var items: MutableMap<String, RollingCoachingTier>,
    var order: MutableList<String>,
) : RollingCoachingTierStore {
    override fun find(code: String): RollingCoachingTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingTier) {
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

    override fun list(): MutableList<RollingCoachingTier> {
        val out = mutableListOf<RollingCoachingTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingTierStore(): RollingCoachingTierStore {
    return MemoryRollingCoachingTierStore(mutableMapOf<String, RollingCoachingTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingTierStore(
    var inner: RollingCoachingTierStore,
    var cache: TtlCache<RollingCoachingTier>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingTierStore {
    override fun find(code: String): RollingCoachingTier? {
        val hit = (cache.get(code) as RollingCoachingTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingTier> = inner.list()
}

fun newCachedRollingCoachingTierStore(inner: RollingCoachingTierStore, ttl: Int, clock: Clock): RollingCoachingTierStore {
    return CachedRollingCoachingTierStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching tiers. Empty fields match all. */
data class RollingCoachingTierQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingTier>): MutableList<RollingCoachingTier> {
        val matched = filter(items) { candidate: RollingCoachingTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingTiers(store: RollingCoachingTierStore, query: RollingCoachingTierQuery): MutableList<RollingCoachingTier> {
    return query.apply(store.list())
}
