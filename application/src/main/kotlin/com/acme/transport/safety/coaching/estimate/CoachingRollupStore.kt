package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching rollups by code. */
interface CoachingRollupStore {
    fun find(code: String): CoachingRollup?
    fun save(item: CoachingRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingRollupStore(
    var items: MutableMap<String, CoachingRollup>,
    var order: MutableList<String>,
) : CoachingRollupStore {
    override fun find(code: String): CoachingRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingRollup) {
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

    override fun list(): MutableList<CoachingRollup> {
        val out = mutableListOf<CoachingRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingRollupStore(): CoachingRollupStore {
    return MemoryCoachingRollupStore(mutableMapOf<String, CoachingRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingRollupStore(
    var inner: CoachingRollupStore,
    var cache: TtlCache<CoachingRollup>,
    var hits: Int,
    var misses: Int,
) : CoachingRollupStore {
    override fun find(code: String): CoachingRollup? {
        val hit = (cache.get(code) as CoachingRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingRollup> = inner.list()
}

fun newCachedCoachingRollupStore(inner: CoachingRollupStore, ttl: Int, clock: Clock): CoachingRollupStore {
    return CachedCoachingRollupStore(
        inner = inner,
        cache = newTtlCache<CoachingRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching rollups. Empty fields match all. */
data class CoachingRollupQuery(
    var region: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingRollup>): MutableList<CoachingRollup> {
        val matched = filter(items) { candidate: CoachingRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingRollups(store: CoachingRollupStore, query: CoachingRollupQuery): MutableList<CoachingRollup> {
    return query.apply(store.list())
}
