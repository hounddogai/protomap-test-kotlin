package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending dock door tiers by code. */
interface PendingDockDoorTierStore {
    fun find(code: String): PendingDockDoorTier?
    fun save(item: PendingDockDoorTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDockDoorTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDockDoorTierStore(
    var items: MutableMap<String, PendingDockDoorTier>,
    var order: MutableList<String>,
) : PendingDockDoorTierStore {
    override fun find(code: String): PendingDockDoorTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDockDoorTier) {
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

    override fun list(): MutableList<PendingDockDoorTier> {
        val out = mutableListOf<PendingDockDoorTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDockDoorTierStore(): PendingDockDoorTierStore {
    return MemoryPendingDockDoorTierStore(mutableMapOf<String, PendingDockDoorTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDockDoorTierStore(
    var inner: PendingDockDoorTierStore,
    var cache: TtlCache<PendingDockDoorTier>,
    var hits: Int,
    var misses: Int,
) : PendingDockDoorTierStore {
    override fun find(code: String): PendingDockDoorTier? {
        val hit = (cache.get(code) as PendingDockDoorTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDockDoorTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDockDoorTier> = inner.list()
}

fun newCachedPendingDockDoorTierStore(inner: PendingDockDoorTierStore, ttl: Int, clock: Clock): PendingDockDoorTierStore {
    return CachedPendingDockDoorTierStore(
        inner = inner,
        cache = newTtlCache<PendingDockDoorTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dock door tiers. Empty fields match all. */
data class PendingDockDoorTierQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDockDoorTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDockDoorTier>): MutableList<PendingDockDoorTier> {
        val matched = filter(items) { candidate: PendingDockDoorTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDockDoorTiers(store: PendingDockDoorTierStore, query: PendingDockDoorTierQuery): MutableList<PendingDockDoorTier> {
    return query.apply(store.list())
}
