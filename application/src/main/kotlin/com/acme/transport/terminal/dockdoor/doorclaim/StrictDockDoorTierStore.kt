package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door tiers by code. */
interface StrictDockDoorTierStore {
    fun find(code: String): StrictDockDoorTier?
    fun save(item: StrictDockDoorTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorTierStore(
    var items: MutableMap<String, StrictDockDoorTier>,
    var order: MutableList<String>,
) : StrictDockDoorTierStore {
    override fun find(code: String): StrictDockDoorTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorTier) {
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

    override fun list(): MutableList<StrictDockDoorTier> {
        val out = mutableListOf<StrictDockDoorTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorTierStore(): StrictDockDoorTierStore {
    return MemoryStrictDockDoorTierStore(mutableMapOf<String, StrictDockDoorTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorTierStore(
    var inner: StrictDockDoorTierStore,
    var cache: TtlCache<StrictDockDoorTier>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorTierStore {
    override fun find(code: String): StrictDockDoorTier? {
        val hit = (cache.get(code) as StrictDockDoorTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorTier> = inner.list()
}

fun newCachedStrictDockDoorTierStore(inner: StrictDockDoorTierStore, ttl: Int, clock: Clock): StrictDockDoorTierStore {
    return CachedStrictDockDoorTierStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door tiers. Empty fields match all. */
data class StrictDockDoorTierQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorTier): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDockDoorTier>): MutableList<StrictDockDoorTier> {
        val matched = filter(items) { candidate: StrictDockDoorTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorTiers(store: StrictDockDoorTierStore, query: StrictDockDoorTierQuery): MutableList<StrictDockDoorTier> {
    return query.apply(store.list())
}
