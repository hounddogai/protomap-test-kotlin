package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict sortation tiers by code. */
interface StrictSortationTierStore {
    fun find(code: String): StrictSortationTier?
    fun save(item: StrictSortationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictSortationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictSortationTierStore(
    var items: MutableMap<String, StrictSortationTier>,
    var order: MutableList<String>,
) : StrictSortationTierStore {
    override fun find(code: String): StrictSortationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictSortationTier) {
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

    override fun list(): MutableList<StrictSortationTier> {
        val out = mutableListOf<StrictSortationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictSortationTierStore(): StrictSortationTierStore {
    return MemoryStrictSortationTierStore(mutableMapOf<String, StrictSortationTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictSortationTierStore(
    var inner: StrictSortationTierStore,
    var cache: TtlCache<StrictSortationTier>,
    var hits: Int,
    var misses: Int,
) : StrictSortationTierStore {
    override fun find(code: String): StrictSortationTier? {
        val hit = (cache.get(code) as StrictSortationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictSortationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictSortationTier> = inner.list()
}

fun newCachedStrictSortationTierStore(inner: StrictSortationTierStore, ttl: Int, clock: Clock): StrictSortationTierStore {
    return CachedStrictSortationTierStore(
        inner = inner,
        cache = newTtlCache<StrictSortationTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict sortation tiers. Empty fields match all. */
data class StrictSortationTierQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictSortationTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictSortationTier>): MutableList<StrictSortationTier> {
        val matched = filter(items) { candidate: StrictSortationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictSortationTiers(store: StrictSortationTierStore, query: StrictSortationTierQuery): MutableList<StrictSortationTier> {
    return query.apply(store.list())
}
