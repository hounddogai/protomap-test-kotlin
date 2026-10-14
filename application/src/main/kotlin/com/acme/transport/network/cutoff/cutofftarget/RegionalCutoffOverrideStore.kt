package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional cutoff overrides by code. */
interface RegionalCutoffOverrideStore {
    fun find(code: String): RegionalCutoffOverride?
    fun save(item: RegionalCutoffOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCutoffOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCutoffOverrideStore(
    var items: MutableMap<String, RegionalCutoffOverride>,
    var order: MutableList<String>,
) : RegionalCutoffOverrideStore {
    override fun find(code: String): RegionalCutoffOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCutoffOverride) {
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

    override fun list(): MutableList<RegionalCutoffOverride> {
        val out = mutableListOf<RegionalCutoffOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCutoffOverrideStore(): RegionalCutoffOverrideStore {
    return MemoryRegionalCutoffOverrideStore(
        mutableMapOf<String, RegionalCutoffOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCutoffOverrideStore(
    var inner: RegionalCutoffOverrideStore,
    var cache: TtlCache<RegionalCutoffOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalCutoffOverrideStore {
    override fun find(code: String): RegionalCutoffOverride? {
        val hit = (cache.get(code) as RegionalCutoffOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCutoffOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCutoffOverride> = inner.list()
}

fun newCachedRegionalCutoffOverrideStore(inner: RegionalCutoffOverrideStore, ttl: Int, clock: Clock): RegionalCutoffOverrideStore {
    return CachedRegionalCutoffOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalCutoffOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cutoff overrides. Empty fields match all. */
data class RegionalCutoffOverrideQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCutoffOverride): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RegionalCutoffOverride>): MutableList<RegionalCutoffOverride> {
        val matched = filter(items) { candidate: RegionalCutoffOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCutoffOverrides(store: RegionalCutoffOverrideStore, query: RegionalCutoffOverrideQuery): MutableList<RegionalCutoffOverride> {
    return query.apply(store.list())
}
