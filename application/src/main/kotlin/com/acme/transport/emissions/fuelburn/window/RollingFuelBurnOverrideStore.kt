package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn overrides by code. */
interface RollingFuelBurnOverrideStore {
    fun find(code: String): RollingFuelBurnOverride?
    fun save(item: RollingFuelBurnOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnOverrideStore(
    var items: MutableMap<String, RollingFuelBurnOverride>,
    var order: MutableList<String>,
) : RollingFuelBurnOverrideStore {
    override fun find(code: String): RollingFuelBurnOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnOverride) {
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

    override fun list(): MutableList<RollingFuelBurnOverride> {
        val out = mutableListOf<RollingFuelBurnOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnOverrideStore(): RollingFuelBurnOverrideStore {
    return MemoryRollingFuelBurnOverrideStore(
        mutableMapOf<String, RollingFuelBurnOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnOverrideStore(
    var inner: RollingFuelBurnOverrideStore,
    var cache: TtlCache<RollingFuelBurnOverride>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnOverrideStore {
    override fun find(code: String): RollingFuelBurnOverride? {
        val hit = (cache.get(code) as RollingFuelBurnOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnOverride> = inner.list()
}

fun newCachedRollingFuelBurnOverrideStore(inner: RollingFuelBurnOverrideStore, ttl: Int, clock: Clock): RollingFuelBurnOverrideStore {
    return CachedRollingFuelBurnOverrideStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn overrides. Empty fields match all. */
data class RollingFuelBurnOverrideQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFuelBurnOverride>): MutableList<RollingFuelBurnOverride> {
        val matched = filter(items) { candidate: RollingFuelBurnOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnOverrides(store: RollingFuelBurnOverrideStore, query: RollingFuelBurnOverrideQuery): MutableList<RollingFuelBurnOverride> {
    return query.apply(store.list())
}
