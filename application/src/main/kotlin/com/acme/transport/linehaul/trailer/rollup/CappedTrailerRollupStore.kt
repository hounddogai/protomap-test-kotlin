package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer rollups by code. */
interface CappedTrailerRollupStore {
    fun find(code: String): CappedTrailerRollup?
    fun save(item: CappedTrailerRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerRollupStore(
    var items: MutableMap<String, CappedTrailerRollup>,
    var order: MutableList<String>,
) : CappedTrailerRollupStore {
    override fun find(code: String): CappedTrailerRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerRollup) {
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

    override fun list(): MutableList<CappedTrailerRollup> {
        val out = mutableListOf<CappedTrailerRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerRollupStore(): CappedTrailerRollupStore {
    return MemoryCappedTrailerRollupStore(mutableMapOf<String, CappedTrailerRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerRollupStore(
    var inner: CappedTrailerRollupStore,
    var cache: TtlCache<CappedTrailerRollup>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerRollupStore {
    override fun find(code: String): CappedTrailerRollup? {
        val hit = (cache.get(code) as CappedTrailerRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerRollup> = inner.list()
}

fun newCachedCappedTrailerRollupStore(inner: CappedTrailerRollupStore, ttl: Int, clock: Clock): CappedTrailerRollupStore {
    return CachedCappedTrailerRollupStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer rollups. Empty fields match all. */
data class CappedTrailerRollupQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerRollup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerRollup>): MutableList<CappedTrailerRollup> {
        val matched = filter(items) { candidate: CappedTrailerRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerRollups(store: CappedTrailerRollupStore, query: CappedTrailerRollupQuery): MutableList<CappedTrailerRollup> {
    return query.apply(store.list())
}
