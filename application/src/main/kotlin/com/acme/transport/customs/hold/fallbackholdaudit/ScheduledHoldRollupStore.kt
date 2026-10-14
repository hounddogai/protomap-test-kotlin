package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold rollups by code. */
interface ScheduledHoldRollupStore {
    fun find(code: String): ScheduledHoldRollup?
    fun save(item: ScheduledHoldRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldRollupStore(
    var items: MutableMap<String, ScheduledHoldRollup>,
    var order: MutableList<String>,
) : ScheduledHoldRollupStore {
    override fun find(code: String): ScheduledHoldRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldRollup) {
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

    override fun list(): MutableList<ScheduledHoldRollup> {
        val out = mutableListOf<ScheduledHoldRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldRollupStore(): ScheduledHoldRollupStore {
    return MemoryScheduledHoldRollupStore(mutableMapOf<String, ScheduledHoldRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldRollupStore(
    var inner: ScheduledHoldRollupStore,
    var cache: TtlCache<ScheduledHoldRollup>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldRollupStore {
    override fun find(code: String): ScheduledHoldRollup? {
        val hit = (cache.get(code) as ScheduledHoldRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldRollup> = inner.list()
}

fun newCachedScheduledHoldRollupStore(inner: ScheduledHoldRollupStore, ttl: Int, clock: Clock): ScheduledHoldRollupStore {
    return CachedScheduledHoldRollupStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold rollups. Empty fields match all. */
data class ScheduledHoldRollupQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldRollup>): MutableList<ScheduledHoldRollup> {
        val matched = filter(items) { candidate: ScheduledHoldRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldRollups(store: ScheduledHoldRollupStore, query: ScheduledHoldRollupQuery): MutableList<ScheduledHoldRollup> {
    return query.apply(store.list())
}
