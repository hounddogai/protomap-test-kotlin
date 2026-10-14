package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door tiers by code. */
interface CappedDockDoorTierStore {
    fun find(code: String): CappedDockDoorTier?
    fun save(item: CappedDockDoorTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorTierStore(
    var items: MutableMap<String, CappedDockDoorTier>,
    var order: MutableList<String>,
) : CappedDockDoorTierStore {
    override fun find(code: String): CappedDockDoorTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorTier) {
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

    override fun list(): MutableList<CappedDockDoorTier> {
        val out = mutableListOf<CappedDockDoorTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorTierStore(): CappedDockDoorTierStore {
    return MemoryCappedDockDoorTierStore(mutableMapOf<String, CappedDockDoorTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorTierStore(
    var inner: CappedDockDoorTierStore,
    var cache: TtlCache<CappedDockDoorTier>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorTierStore {
    override fun find(code: String): CappedDockDoorTier? {
        val hit = (cache.get(code) as CappedDockDoorTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorTier> = inner.list()
}

fun newCachedCappedDockDoorTierStore(inner: CappedDockDoorTierStore, ttl: Int, clock: Clock): CappedDockDoorTierStore {
    return CachedCappedDockDoorTierStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door tiers. Empty fields match all. */
data class CappedDockDoorTierQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDockDoorTier>): MutableList<CappedDockDoorTier> {
        val matched = filter(items) { candidate: CappedDockDoorTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorTiers(store: CappedDockDoorTierStore, query: CappedDockDoorTierQuery): MutableList<CappedDockDoorTier> {
    return query.apply(store.list())
}
