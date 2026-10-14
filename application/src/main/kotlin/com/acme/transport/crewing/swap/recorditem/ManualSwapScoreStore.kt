package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual swap scores by code. */
interface ManualSwapScoreStore {
    fun find(code: String): ManualSwapScore?
    fun save(item: ManualSwapScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualSwapScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualSwapScoreStore(
    var items: MutableMap<String, ManualSwapScore>,
    var order: MutableList<String>,
) : ManualSwapScoreStore {
    override fun find(code: String): ManualSwapScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualSwapScore) {
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

    override fun list(): MutableList<ManualSwapScore> {
        val out = mutableListOf<ManualSwapScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualSwapScoreStore(): ManualSwapScoreStore {
    return MemoryManualSwapScoreStore(mutableMapOf<String, ManualSwapScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualSwapScoreStore(
    var inner: ManualSwapScoreStore,
    var cache: TtlCache<ManualSwapScore>,
    var hits: Int,
    var misses: Int,
) : ManualSwapScoreStore {
    override fun find(code: String): ManualSwapScore? {
        val hit = (cache.get(code) as ManualSwapScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualSwapScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualSwapScore> = inner.list()
}

fun newCachedManualSwapScoreStore(inner: ManualSwapScoreStore, ttl: Int, clock: Clock): ManualSwapScoreStore {
    return CachedManualSwapScoreStore(
        inner = inner,
        cache = newTtlCache<ManualSwapScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual swap scores. Empty fields match all. */
data class ManualSwapScoreQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualSwapScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualSwapScore>): MutableList<ManualSwapScore> {
        val matched = filter(items) { candidate: ManualSwapScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualSwapScores(store: ManualSwapScoreStore, query: ManualSwapScoreQuery): MutableList<ManualSwapScore> {
    return query.apply(store.list())
}
