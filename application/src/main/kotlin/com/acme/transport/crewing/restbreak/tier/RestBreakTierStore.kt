package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break tiers by code. */
interface RestBreakTierStore {
    fun find(code: String): RestBreakTier?
    fun save(item: RestBreakTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakTierStore(
    var items: MutableMap<String, RestBreakTier>,
    var order: MutableList<String>,
) : RestBreakTierStore {
    override fun find(code: String): RestBreakTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakTier) {
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

    override fun list(): MutableList<RestBreakTier> {
        val out = mutableListOf<RestBreakTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakTierStore(): RestBreakTierStore {
    return MemoryRestBreakTierStore(mutableMapOf<String, RestBreakTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakTierStore(
    var inner: RestBreakTierStore,
    var cache: TtlCache<RestBreakTier>,
    var hits: Int,
    var misses: Int,
) : RestBreakTierStore {
    override fun find(code: String): RestBreakTier? {
        val hit = (cache.get(code) as RestBreakTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakTier> = inner.list()
}

fun newCachedRestBreakTierStore(inner: RestBreakTierStore, ttl: Int, clock: Clock): RestBreakTierStore {
    return CachedRestBreakTierStore(
        inner = inner,
        cache = newTtlCache<RestBreakTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break tiers. Empty fields match all. */
data class RestBreakTierQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakTier>): MutableList<RestBreakTier> {
        val matched = filter(items) { candidate: RestBreakTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakTiers(store: RestBreakTierStore, query: RestBreakTierQuery): MutableList<RestBreakTier> {
    return query.apply(store.list())
}
