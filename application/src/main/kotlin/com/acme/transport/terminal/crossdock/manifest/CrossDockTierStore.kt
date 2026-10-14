package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock tiers by code. */
interface CrossDockTierStore {
    fun find(code: String): CrossDockTier?
    fun save(item: CrossDockTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockTierStore(
    var items: MutableMap<String, CrossDockTier>,
    var order: MutableList<String>,
) : CrossDockTierStore {
    override fun find(code: String): CrossDockTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockTier) {
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

    override fun list(): MutableList<CrossDockTier> {
        val out = mutableListOf<CrossDockTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockTierStore(): CrossDockTierStore {
    return MemoryCrossDockTierStore(mutableMapOf<String, CrossDockTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockTierStore(
    var inner: CrossDockTierStore,
    var cache: TtlCache<CrossDockTier>,
    var hits: Int,
    var misses: Int,
) : CrossDockTierStore {
    override fun find(code: String): CrossDockTier? {
        val hit = (cache.get(code) as CrossDockTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockTier> = inner.list()
}

fun newCachedCrossDockTierStore(inner: CrossDockTierStore, ttl: Int, clock: Clock): CrossDockTierStore {
    return CachedCrossDockTierStore(
        inner = inner,
        cache = newTtlCache<CrossDockTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock tiers. Empty fields match all. */
data class CrossDockTierQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockTier): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockTier>): MutableList<CrossDockTier> {
        val matched = filter(items) { candidate: CrossDockTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockTiers(store: CrossDockTierStore, query: CrossDockTierQuery): MutableList<CrossDockTier> {
    return query.apply(store.list())
}
