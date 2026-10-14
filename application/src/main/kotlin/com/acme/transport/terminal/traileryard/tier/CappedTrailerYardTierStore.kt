package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped trailer yard tiers by code. */
interface CappedTrailerYardTierStore {
    fun find(code: String): CappedTrailerYardTier?
    fun save(item: CappedTrailerYardTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerYardTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerYardTierStore(
    var items: MutableMap<String, CappedTrailerYardTier>,
    var order: MutableList<String>,
) : CappedTrailerYardTierStore {
    override fun find(code: String): CappedTrailerYardTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerYardTier) {
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

    override fun list(): MutableList<CappedTrailerYardTier> {
        val out = mutableListOf<CappedTrailerYardTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerYardTierStore(): CappedTrailerYardTierStore {
    return MemoryCappedTrailerYardTierStore(
        mutableMapOf<String, CappedTrailerYardTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerYardTierStore(
    var inner: CappedTrailerYardTierStore,
    var cache: TtlCache<CappedTrailerYardTier>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerYardTierStore {
    override fun find(code: String): CappedTrailerYardTier? {
        val hit = (cache.get(code) as CappedTrailerYardTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerYardTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerYardTier> = inner.list()
}

fun newCachedCappedTrailerYardTierStore(inner: CappedTrailerYardTierStore, ttl: Int, clock: Clock): CappedTrailerYardTierStore {
    return CachedCappedTrailerYardTierStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerYardTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer yard tiers. Empty fields match all. */
data class CappedTrailerYardTierQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerYardTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CappedTrailerYardTier>): MutableList<CappedTrailerYardTier> {
        val matched = filter(items) { candidate: CappedTrailerYardTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerYardTiers(store: CappedTrailerYardTierStore, query: CappedTrailerYardTierQuery): MutableList<CappedTrailerYardTier> {
    return query.apply(store.list())
}
