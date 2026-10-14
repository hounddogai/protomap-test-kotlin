package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional clearance overrides by code. */
interface RegionalClearanceOverrideStore {
    fun find(code: String): RegionalClearanceOverride?
    fun save(item: RegionalClearanceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalClearanceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalClearanceOverrideStore(
    var items: MutableMap<String, RegionalClearanceOverride>,
    var order: MutableList<String>,
) : RegionalClearanceOverrideStore {
    override fun find(code: String): RegionalClearanceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalClearanceOverride) {
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

    override fun list(): MutableList<RegionalClearanceOverride> {
        val out = mutableListOf<RegionalClearanceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalClearanceOverrideStore(): RegionalClearanceOverrideStore {
    return MemoryRegionalClearanceOverrideStore(
        mutableMapOf<String, RegionalClearanceOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalClearanceOverrideStore(
    var inner: RegionalClearanceOverrideStore,
    var cache: TtlCache<RegionalClearanceOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalClearanceOverrideStore {
    override fun find(code: String): RegionalClearanceOverride? {
        val hit = (cache.get(code) as RegionalClearanceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalClearanceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalClearanceOverride> = inner.list()
}

fun newCachedRegionalClearanceOverrideStore(inner: RegionalClearanceOverrideStore, ttl: Int, clock: Clock): RegionalClearanceOverrideStore {
    return CachedRegionalClearanceOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalClearanceOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional clearance overrides. Empty fields match all. */
data class RegionalClearanceOverrideQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalClearanceOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RegionalClearanceOverride>): MutableList<RegionalClearanceOverride> {
        val matched = filter(items) { candidate: RegionalClearanceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalClearanceOverrides(store: RegionalClearanceOverrideStore, query: RegionalClearanceOverrideQuery): MutableList<RegionalClearanceOverride> {
    return query.apply(store.list())
}
