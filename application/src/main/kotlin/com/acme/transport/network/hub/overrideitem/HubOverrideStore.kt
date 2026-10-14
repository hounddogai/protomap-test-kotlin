package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub overrides by code. */
interface HubOverrideStore {
    fun find(code: String): HubOverride?
    fun save(item: HubOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubOverrideStore(
    var items: MutableMap<String, HubOverride>,
    var order: MutableList<String>,
) : HubOverrideStore {
    override fun find(code: String): HubOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubOverride) {
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

    override fun list(): MutableList<HubOverride> {
        val out = mutableListOf<HubOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubOverrideStore(): HubOverrideStore {
    return MemoryHubOverrideStore(mutableMapOf<String, HubOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubOverrideStore(
    var inner: HubOverrideStore,
    var cache: TtlCache<HubOverride>,
    var hits: Int,
    var misses: Int,
) : HubOverrideStore {
    override fun find(code: String): HubOverride? {
        val hit = (cache.get(code) as HubOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubOverride> = inner.list()
}

fun newCachedHubOverrideStore(inner: HubOverrideStore, ttl: Int, clock: Clock): HubOverrideStore {
    return CachedHubOverrideStore(
        inner = inner,
        cache = newTtlCache<HubOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub overrides. Empty fields match all. */
data class HubOverrideQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubOverride): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubOverride>): MutableList<HubOverride> {
        val matched = filter(items) { candidate: HubOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubOverrides(store: HubOverrideStore, query: HubOverrideQuery): MutableList<HubOverride> {
    return query.apply(store.list())
}
