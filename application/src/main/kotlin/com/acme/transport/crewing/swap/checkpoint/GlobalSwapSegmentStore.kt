package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global swap segments by code. */
interface GlobalSwapSegmentStore {
    fun find(code: String): GlobalSwapSegment?
    fun save(item: GlobalSwapSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSwapSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSwapSegmentStore(
    var items: MutableMap<String, GlobalSwapSegment>,
    var order: MutableList<String>,
) : GlobalSwapSegmentStore {
    override fun find(code: String): GlobalSwapSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSwapSegment) {
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

    override fun list(): MutableList<GlobalSwapSegment> {
        val out = mutableListOf<GlobalSwapSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSwapSegmentStore(): GlobalSwapSegmentStore {
    return MemoryGlobalSwapSegmentStore(mutableMapOf<String, GlobalSwapSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSwapSegmentStore(
    var inner: GlobalSwapSegmentStore,
    var cache: TtlCache<GlobalSwapSegment>,
    var hits: Int,
    var misses: Int,
) : GlobalSwapSegmentStore {
    override fun find(code: String): GlobalSwapSegment? {
        val hit = (cache.get(code) as GlobalSwapSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSwapSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSwapSegment> = inner.list()
}

fun newCachedGlobalSwapSegmentStore(inner: GlobalSwapSegmentStore, ttl: Int, clock: Clock): GlobalSwapSegmentStore {
    return CachedGlobalSwapSegmentStore(
        inner = inner,
        cache = newTtlCache<GlobalSwapSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global swap segments. Empty fields match all. */
data class GlobalSwapSegmentQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSwapSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSwapSegment>): MutableList<GlobalSwapSegment> {
        val matched = filter(items) { candidate: GlobalSwapSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSwapSegments(store: GlobalSwapSegmentStore, query: GlobalSwapSegmentQuery): MutableList<GlobalSwapSegment> {
    return query.apply(store.list())
}
