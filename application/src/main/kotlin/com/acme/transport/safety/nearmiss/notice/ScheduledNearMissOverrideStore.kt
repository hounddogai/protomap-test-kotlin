package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss overrides by code. */
interface ScheduledNearMissOverrideStore {
    fun find(code: String): ScheduledNearMissOverride?
    fun save(item: ScheduledNearMissOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissOverrideStore(
    var items: MutableMap<String, ScheduledNearMissOverride>,
    var order: MutableList<String>,
) : ScheduledNearMissOverrideStore {
    override fun find(code: String): ScheduledNearMissOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissOverride) {
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

    override fun list(): MutableList<ScheduledNearMissOverride> {
        val out = mutableListOf<ScheduledNearMissOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissOverrideStore(): ScheduledNearMissOverrideStore {
    return MemoryScheduledNearMissOverrideStore(
        mutableMapOf<String, ScheduledNearMissOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissOverrideStore(
    var inner: ScheduledNearMissOverrideStore,
    var cache: TtlCache<ScheduledNearMissOverride>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissOverrideStore {
    override fun find(code: String): ScheduledNearMissOverride? {
        val hit = (cache.get(code) as ScheduledNearMissOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissOverride> = inner.list()
}

fun newCachedScheduledNearMissOverrideStore(inner: ScheduledNearMissOverrideStore, ttl: Int, clock: Clock): ScheduledNearMissOverrideStore {
    return CachedScheduledNearMissOverrideStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss overrides. Empty fields match all. */
data class ScheduledNearMissOverrideQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNearMissOverride>): MutableList<ScheduledNearMissOverride> {
        val matched = filter(items) { candidate: ScheduledNearMissOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissOverrides(store: ScheduledNearMissOverrideStore, query: ScheduledNearMissOverrideQuery): MutableList<ScheduledNearMissOverride> {
    return query.apply(store.list())
}
