package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster overrides by code. */
interface ScheduledRosterOverrideStore {
    fun find(code: String): ScheduledRosterOverride?
    fun save(item: ScheduledRosterOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterOverrideStore(
    var items: MutableMap<String, ScheduledRosterOverride>,
    var order: MutableList<String>,
) : ScheduledRosterOverrideStore {
    override fun find(code: String): ScheduledRosterOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterOverride) {
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

    override fun list(): MutableList<ScheduledRosterOverride> {
        val out = mutableListOf<ScheduledRosterOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterOverrideStore(): ScheduledRosterOverrideStore {
    return MemoryScheduledRosterOverrideStore(
        mutableMapOf<String, ScheduledRosterOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterOverrideStore(
    var inner: ScheduledRosterOverrideStore,
    var cache: TtlCache<ScheduledRosterOverride>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterOverrideStore {
    override fun find(code: String): ScheduledRosterOverride? {
        val hit = (cache.get(code) as ScheduledRosterOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterOverride> = inner.list()
}

fun newCachedScheduledRosterOverrideStore(inner: ScheduledRosterOverrideStore, ttl: Int, clock: Clock): ScheduledRosterOverrideStore {
    return CachedScheduledRosterOverrideStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster overrides. Empty fields match all. */
data class ScheduledRosterOverrideQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRosterOverride>): MutableList<ScheduledRosterOverride> {
        val matched = filter(items) { candidate: ScheduledRosterOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterOverrides(store: ScheduledRosterOverrideStore, query: ScheduledRosterOverrideQuery): MutableList<ScheduledRosterOverride> {
    return query.apply(store.list())
}
