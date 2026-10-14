package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer tiers by code. */
interface TrailerTierStore {
    fun find(code: String): TrailerTier?
    fun save(item: TrailerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerTierStore(
    var items: MutableMap<String, TrailerTier>,
    var order: MutableList<String>,
) : TrailerTierStore {
    override fun find(code: String): TrailerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerTier) {
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

    override fun list(): MutableList<TrailerTier> {
        val out = mutableListOf<TrailerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerTierStore(): TrailerTierStore {
    return MemoryTrailerTierStore(mutableMapOf<String, TrailerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerTierStore(
    var inner: TrailerTierStore,
    var cache: TtlCache<TrailerTier>,
    var hits: Int,
    var misses: Int,
) : TrailerTierStore {
    override fun find(code: String): TrailerTier? {
        val hit = (cache.get(code) as TrailerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerTier> = inner.list()
}

fun newCachedTrailerTierStore(inner: TrailerTierStore, ttl: Int, clock: Clock): TrailerTierStore {
    return CachedTrailerTierStore(
        inner = inner,
        cache = newTtlCache<TrailerTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer tiers. Empty fields match all. */
data class TrailerTierQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TrailerTier>): MutableList<TrailerTier> {
        val matched = filter(items) { candidate: TrailerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerTiers(store: TrailerTierStore, query: TrailerTierQuery): MutableList<TrailerTier> {
    return query.apply(store.list())
}
