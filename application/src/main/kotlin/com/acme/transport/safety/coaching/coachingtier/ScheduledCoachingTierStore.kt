package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled coaching tiers by code. */
interface ScheduledCoachingTierStore {
    fun find(code: String): ScheduledCoachingTier?
    fun save(item: ScheduledCoachingTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCoachingTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCoachingTierStore(
    var items: MutableMap<String, ScheduledCoachingTier>,
    var order: MutableList<String>,
) : ScheduledCoachingTierStore {
    override fun find(code: String): ScheduledCoachingTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCoachingTier) {
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

    override fun list(): MutableList<ScheduledCoachingTier> {
        val out = mutableListOf<ScheduledCoachingTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCoachingTierStore(): ScheduledCoachingTierStore {
    return MemoryScheduledCoachingTierStore(
        mutableMapOf<String, ScheduledCoachingTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCoachingTierStore(
    var inner: ScheduledCoachingTierStore,
    var cache: TtlCache<ScheduledCoachingTier>,
    var hits: Int,
    var misses: Int,
) : ScheduledCoachingTierStore {
    override fun find(code: String): ScheduledCoachingTier? {
        val hit = (cache.get(code) as ScheduledCoachingTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCoachingTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCoachingTier> = inner.list()
}

fun newCachedScheduledCoachingTierStore(inner: ScheduledCoachingTierStore, ttl: Int, clock: Clock): ScheduledCoachingTierStore {
    return CachedScheduledCoachingTierStore(
        inner = inner,
        cache = newTtlCache<ScheduledCoachingTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled coaching tiers. Empty fields match all. */
data class ScheduledCoachingTierQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCoachingTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCoachingTier>): MutableList<ScheduledCoachingTier> {
        val matched = filter(items) { candidate: ScheduledCoachingTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCoachingTiers(store: ScheduledCoachingTierStore, query: ScheduledCoachingTierQuery): MutableList<ScheduledCoachingTier> {
    return query.apply(store.list())
}
