package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional sortation overrides by code. */
interface RegionalSortationOverrideStore {
    fun find(code: String): RegionalSortationOverride?
    fun save(item: RegionalSortationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSortationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSortationOverrideStore(
    var items: MutableMap<String, RegionalSortationOverride>,
    var order: MutableList<String>,
) : RegionalSortationOverrideStore {
    override fun find(code: String): RegionalSortationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSortationOverride) {
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

    override fun list(): MutableList<RegionalSortationOverride> {
        val out = mutableListOf<RegionalSortationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSortationOverrideStore(): RegionalSortationOverrideStore {
    return MemoryRegionalSortationOverrideStore(
        mutableMapOf<String, RegionalSortationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSortationOverrideStore(
    var inner: RegionalSortationOverrideStore,
    var cache: TtlCache<RegionalSortationOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalSortationOverrideStore {
    override fun find(code: String): RegionalSortationOverride? {
        val hit = (cache.get(code) as RegionalSortationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSortationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSortationOverride> = inner.list()
}

fun newCachedRegionalSortationOverrideStore(inner: RegionalSortationOverrideStore, ttl: Int, clock: Clock): RegionalSortationOverrideStore {
    return CachedRegionalSortationOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalSortationOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional sortation overrides. Empty fields match all. */
data class RegionalSortationOverrideQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSortationOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalSortationOverride>): MutableList<RegionalSortationOverride> {
        val matched = filter(items) { candidate: RegionalSortationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSortationOverrides(store: RegionalSortationOverrideStore, query: RegionalSortationOverrideQuery): MutableList<RegionalSortationOverride> {
    return query.apply(store.list())
}
