package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary zone tiers by code. */
interface PrimaryZoneTierStore {
    fun find(code: String): PrimaryZoneTier?
    fun save(item: PrimaryZoneTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryZoneTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryZoneTierStore(
    var items: MutableMap<String, PrimaryZoneTier>,
    var order: MutableList<String>,
) : PrimaryZoneTierStore {
    override fun find(code: String): PrimaryZoneTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryZoneTier) {
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

    override fun list(): MutableList<PrimaryZoneTier> {
        val out = mutableListOf<PrimaryZoneTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryZoneTierStore(): PrimaryZoneTierStore {
    return MemoryPrimaryZoneTierStore(mutableMapOf<String, PrimaryZoneTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryZoneTierStore(
    var inner: PrimaryZoneTierStore,
    var cache: TtlCache<PrimaryZoneTier>,
    var hits: Int,
    var misses: Int,
) : PrimaryZoneTierStore {
    override fun find(code: String): PrimaryZoneTier? {
        val hit = (cache.get(code) as PrimaryZoneTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryZoneTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryZoneTier> = inner.list()
}

fun newCachedPrimaryZoneTierStore(inner: PrimaryZoneTierStore, ttl: Int, clock: Clock): PrimaryZoneTierStore {
    return CachedPrimaryZoneTierStore(
        inner = inner,
        cache = newTtlCache<PrimaryZoneTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary zone tiers. Empty fields match all. */
data class PrimaryZoneTierQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryZoneTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryZoneTier>): MutableList<PrimaryZoneTier> {
        val matched = filter(items) { candidate: PrimaryZoneTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryZoneTiers(store: PrimaryZoneTierStore, query: PrimaryZoneTierQuery): MutableList<PrimaryZoneTier> {
    return query.apply(store.list())
}
