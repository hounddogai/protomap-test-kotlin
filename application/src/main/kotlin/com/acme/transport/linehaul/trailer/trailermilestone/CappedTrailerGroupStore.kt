package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer groups by code. */
interface CappedTrailerGroupStore {
    fun find(code: String): CappedTrailerGroup?
    fun save(item: CappedTrailerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerGroupStore(
    var items: MutableMap<String, CappedTrailerGroup>,
    var order: MutableList<String>,
) : CappedTrailerGroupStore {
    override fun find(code: String): CappedTrailerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerGroup) {
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

    override fun list(): MutableList<CappedTrailerGroup> {
        val out = mutableListOf<CappedTrailerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerGroupStore(): CappedTrailerGroupStore {
    return MemoryCappedTrailerGroupStore(mutableMapOf<String, CappedTrailerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerGroupStore(
    var inner: CappedTrailerGroupStore,
    var cache: TtlCache<CappedTrailerGroup>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerGroupStore {
    override fun find(code: String): CappedTrailerGroup? {
        val hit = (cache.get(code) as CappedTrailerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerGroup> = inner.list()
}

fun newCachedCappedTrailerGroupStore(inner: CappedTrailerGroupStore, ttl: Int, clock: Clock): CappedTrailerGroupStore {
    return CachedCappedTrailerGroupStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer groups. Empty fields match all. */
data class CappedTrailerGroupQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerGroup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerGroup>): MutableList<CappedTrailerGroup> {
        val matched = filter(items) { candidate: CappedTrailerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerGroups(store: CappedTrailerGroupStore, query: CappedTrailerGroupQuery): MutableList<CappedTrailerGroup> {
    return query.apply(store.list())
}
