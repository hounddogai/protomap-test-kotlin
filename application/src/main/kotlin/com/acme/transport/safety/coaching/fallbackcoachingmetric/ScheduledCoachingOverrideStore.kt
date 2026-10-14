package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled coaching overrides by code. */
interface ScheduledCoachingOverrideStore {
    fun find(code: String): ScheduledCoachingOverride?
    fun save(item: ScheduledCoachingOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCoachingOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCoachingOverrideStore(
    var items: MutableMap<String, ScheduledCoachingOverride>,
    var order: MutableList<String>,
) : ScheduledCoachingOverrideStore {
    override fun find(code: String): ScheduledCoachingOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCoachingOverride) {
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

    override fun list(): MutableList<ScheduledCoachingOverride> {
        val out = mutableListOf<ScheduledCoachingOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCoachingOverrideStore(): ScheduledCoachingOverrideStore {
    return MemoryScheduledCoachingOverrideStore(
        mutableMapOf<String, ScheduledCoachingOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCoachingOverrideStore(
    var inner: ScheduledCoachingOverrideStore,
    var cache: TtlCache<ScheduledCoachingOverride>,
    var hits: Int,
    var misses: Int,
) : ScheduledCoachingOverrideStore {
    override fun find(code: String): ScheduledCoachingOverride? {
        val hit = (cache.get(code) as ScheduledCoachingOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCoachingOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCoachingOverride> = inner.list()
}

fun newCachedScheduledCoachingOverrideStore(inner: ScheduledCoachingOverrideStore, ttl: Int, clock: Clock): ScheduledCoachingOverrideStore {
    return CachedScheduledCoachingOverrideStore(
        inner = inner,
        cache = newTtlCache<ScheduledCoachingOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled coaching overrides. Empty fields match all. */
data class ScheduledCoachingOverrideQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCoachingOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCoachingOverride>): MutableList<ScheduledCoachingOverride> {
        val matched = filter(items) { candidate: ScheduledCoachingOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCoachingOverrides(store: ScheduledCoachingOverrideStore, query: ScheduledCoachingOverrideQuery): MutableList<ScheduledCoachingOverride> {
    return query.apply(store.list())
}
