package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped trailer yard rollups by code. */
interface CappedTrailerYardRollupStore {
    fun find(code: String): CappedTrailerYardRollup?
    fun save(item: CappedTrailerYardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerYardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerYardRollupStore(
    var items: MutableMap<String, CappedTrailerYardRollup>,
    var order: MutableList<String>,
) : CappedTrailerYardRollupStore {
    override fun find(code: String): CappedTrailerYardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerYardRollup) {
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

    override fun list(): MutableList<CappedTrailerYardRollup> {
        val out = mutableListOf<CappedTrailerYardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerYardRollupStore(): CappedTrailerYardRollupStore {
    return MemoryCappedTrailerYardRollupStore(
        mutableMapOf<String, CappedTrailerYardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerYardRollupStore(
    var inner: CappedTrailerYardRollupStore,
    var cache: TtlCache<CappedTrailerYardRollup>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerYardRollupStore {
    override fun find(code: String): CappedTrailerYardRollup? {
        val hit = (cache.get(code) as CappedTrailerYardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerYardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerYardRollup> = inner.list()
}

fun newCachedCappedTrailerYardRollupStore(inner: CappedTrailerYardRollupStore, ttl: Int, clock: Clock): CappedTrailerYardRollupStore {
    return CachedCappedTrailerYardRollupStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerYardRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer yard rollups. Empty fields match all. */
data class CappedTrailerYardRollupQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerYardRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<CappedTrailerYardRollup>): MutableList<CappedTrailerYardRollup> {
        val matched = filter(items) { candidate: CappedTrailerYardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerYardRollups(store: CappedTrailerYardRollupStore, query: CappedTrailerYardRollupQuery): MutableList<CappedTrailerYardRollup> {
    return query.apply(store.list())
}
