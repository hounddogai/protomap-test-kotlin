package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling capacity rollups by code. */
interface RollingCapacityRollupStore {
    fun find(code: String): RollingCapacityRollup?
    fun save(item: RollingCapacityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCapacityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCapacityRollupStore(
    var items: MutableMap<String, RollingCapacityRollup>,
    var order: MutableList<String>,
) : RollingCapacityRollupStore {
    override fun find(code: String): RollingCapacityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCapacityRollup) {
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

    override fun list(): MutableList<RollingCapacityRollup> {
        val out = mutableListOf<RollingCapacityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCapacityRollupStore(): RollingCapacityRollupStore {
    return MemoryRollingCapacityRollupStore(
        mutableMapOf<String, RollingCapacityRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCapacityRollupStore(
    var inner: RollingCapacityRollupStore,
    var cache: TtlCache<RollingCapacityRollup>,
    var hits: Int,
    var misses: Int,
) : RollingCapacityRollupStore {
    override fun find(code: String): RollingCapacityRollup? {
        val hit = (cache.get(code) as RollingCapacityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCapacityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCapacityRollup> = inner.list()
}

fun newCachedRollingCapacityRollupStore(inner: RollingCapacityRollupStore, ttl: Int, clock: Clock): RollingCapacityRollupStore {
    return CachedRollingCapacityRollupStore(
        inner = inner,
        cache = newTtlCache<RollingCapacityRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling capacity rollups. Empty fields match all. */
data class RollingCapacityRollupQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCapacityRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCapacityRollup>): MutableList<RollingCapacityRollup> {
        val matched = filter(items) { candidate: RollingCapacityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCapacityRollups(store: RollingCapacityRollupStore, query: RollingCapacityRollupQuery): MutableList<RollingCapacityRollup> {
    return query.apply(store.list())
}
