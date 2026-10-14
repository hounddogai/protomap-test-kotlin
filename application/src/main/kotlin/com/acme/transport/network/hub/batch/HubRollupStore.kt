package com.acme.transport.network.hub.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub rollups by code. */
interface HubRollupStore {
    fun find(code: String): HubRollup?
    fun save(item: HubRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubRollupStore(
    var items: MutableMap<String, HubRollup>,
    var order: MutableList<String>,
) : HubRollupStore {
    override fun find(code: String): HubRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubRollup) {
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

    override fun list(): MutableList<HubRollup> {
        val out = mutableListOf<HubRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubRollupStore(): HubRollupStore {
    return MemoryHubRollupStore(mutableMapOf<String, HubRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubRollupStore(
    var inner: HubRollupStore,
    var cache: TtlCache<HubRollup>,
    var hits: Int,
    var misses: Int,
) : HubRollupStore {
    override fun find(code: String): HubRollup? {
        val hit = (cache.get(code) as HubRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubRollup> = inner.list()
}

fun newCachedHubRollupStore(inner: HubRollupStore, ttl: Int, clock: Clock): HubRollupStore {
    return CachedHubRollupStore(
        inner = inner,
        cache = newTtlCache<HubRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub rollups. Empty fields match all. */
data class HubRollupQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubRollup>): MutableList<HubRollup> {
        val matched = filter(items) { candidate: HubRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubRollups(store: HubRollupStore, query: HubRollupQuery): MutableList<HubRollup> {
    return query.apply(store.list())
}
