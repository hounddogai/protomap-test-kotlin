package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling roster rollups by code. */
interface RollingRosterRollupStore {
    fun find(code: String): RollingRosterRollup?
    fun save(item: RollingRosterRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRosterRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRosterRollupStore(
    var items: MutableMap<String, RollingRosterRollup>,
    var order: MutableList<String>,
) : RollingRosterRollupStore {
    override fun find(code: String): RollingRosterRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRosterRollup) {
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

    override fun list(): MutableList<RollingRosterRollup> {
        val out = mutableListOf<RollingRosterRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRosterRollupStore(): RollingRosterRollupStore {
    return MemoryRollingRosterRollupStore(mutableMapOf<String, RollingRosterRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRosterRollupStore(
    var inner: RollingRosterRollupStore,
    var cache: TtlCache<RollingRosterRollup>,
    var hits: Int,
    var misses: Int,
) : RollingRosterRollupStore {
    override fun find(code: String): RollingRosterRollup? {
        val hit = (cache.get(code) as RollingRosterRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRosterRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRosterRollup> = inner.list()
}

fun newCachedRollingRosterRollupStore(inner: RollingRosterRollupStore, ttl: Int, clock: Clock): RollingRosterRollupStore {
    return CachedRollingRosterRollupStore(
        inner = inner,
        cache = newTtlCache<RollingRosterRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling roster rollups. Empty fields match all. */
data class RollingRosterRollupQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRosterRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RollingRosterRollup>): MutableList<RollingRosterRollup> {
        val matched = filter(items) { candidate: RollingRosterRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRosterRollups(store: RollingRosterRollupStore, query: RollingRosterRollupQuery): MutableList<RollingRosterRollup> {
    return query.apply(store.list())
}
