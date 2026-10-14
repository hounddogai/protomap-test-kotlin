package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling trailer groups by code. */
interface RollingTrailerGroupStore {
    fun find(code: String): RollingTrailerGroup?
    fun save(item: RollingTrailerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerGroupStore(
    var items: MutableMap<String, RollingTrailerGroup>,
    var order: MutableList<String>,
) : RollingTrailerGroupStore {
    override fun find(code: String): RollingTrailerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerGroup) {
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

    override fun list(): MutableList<RollingTrailerGroup> {
        val out = mutableListOf<RollingTrailerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerGroupStore(): RollingTrailerGroupStore {
    return MemoryRollingTrailerGroupStore(mutableMapOf<String, RollingTrailerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerGroupStore(
    var inner: RollingTrailerGroupStore,
    var cache: TtlCache<RollingTrailerGroup>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerGroupStore {
    override fun find(code: String): RollingTrailerGroup? {
        val hit = (cache.get(code) as RollingTrailerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerGroup> = inner.list()
}

fun newCachedRollingTrailerGroupStore(inner: RollingTrailerGroupStore, ttl: Int, clock: Clock): RollingTrailerGroupStore {
    return CachedRollingTrailerGroupStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer groups. Empty fields match all. */
data class RollingTrailerGroupQuery(
    var depot: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<RollingTrailerGroup>): MutableList<RollingTrailerGroup> {
        val matched = filter(items) { candidate: RollingTrailerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerGroups(store: RollingTrailerGroupStore, query: RollingTrailerGroupQuery): MutableList<RollingTrailerGroup> {
    return query.apply(store.list())
}
