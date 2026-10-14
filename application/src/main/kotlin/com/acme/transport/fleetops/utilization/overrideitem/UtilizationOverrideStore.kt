package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization overrides by code. */
interface UtilizationOverrideStore {
    fun find(code: String): UtilizationOverride?
    fun save(item: UtilizationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationOverrideStore(
    var items: MutableMap<String, UtilizationOverride>,
    var order: MutableList<String>,
) : UtilizationOverrideStore {
    override fun find(code: String): UtilizationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationOverride) {
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

    override fun list(): MutableList<UtilizationOverride> {
        val out = mutableListOf<UtilizationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationOverrideStore(): UtilizationOverrideStore {
    return MemoryUtilizationOverrideStore(mutableMapOf<String, UtilizationOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationOverrideStore(
    var inner: UtilizationOverrideStore,
    var cache: TtlCache<UtilizationOverride>,
    var hits: Int,
    var misses: Int,
) : UtilizationOverrideStore {
    override fun find(code: String): UtilizationOverride? {
        val hit = (cache.get(code) as UtilizationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationOverride> = inner.list()
}

fun newCachedUtilizationOverrideStore(inner: UtilizationOverrideStore, ttl: Int, clock: Clock): UtilizationOverrideStore {
    return CachedUtilizationOverrideStore(
        inner = inner,
        cache = newTtlCache<UtilizationOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization overrides. Empty fields match all. */
data class UtilizationOverrideQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<UtilizationOverride>): MutableList<UtilizationOverride> {
        val matched = filter(items) { candidate: UtilizationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationOverrides(store: UtilizationOverrideStore, query: UtilizationOverrideQuery): MutableList<UtilizationOverride> {
    return query.apply(store.list())
}
