package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending cutoff scores by code. */
interface PendingCutoffScoreStore {
    fun find(code: String): PendingCutoffScore?
    fun save(item: PendingCutoffScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCutoffScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCutoffScoreStore(
    var items: MutableMap<String, PendingCutoffScore>,
    var order: MutableList<String>,
) : PendingCutoffScoreStore {
    override fun find(code: String): PendingCutoffScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCutoffScore) {
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

    override fun list(): MutableList<PendingCutoffScore> {
        val out = mutableListOf<PendingCutoffScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCutoffScoreStore(): PendingCutoffScoreStore {
    return MemoryPendingCutoffScoreStore(mutableMapOf<String, PendingCutoffScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCutoffScoreStore(
    var inner: PendingCutoffScoreStore,
    var cache: TtlCache<PendingCutoffScore>,
    var hits: Int,
    var misses: Int,
) : PendingCutoffScoreStore {
    override fun find(code: String): PendingCutoffScore? {
        val hit = (cache.get(code) as PendingCutoffScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCutoffScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCutoffScore> = inner.list()
}

fun newCachedPendingCutoffScoreStore(inner: PendingCutoffScoreStore, ttl: Int, clock: Clock): PendingCutoffScoreStore {
    return CachedPendingCutoffScoreStore(
        inner = inner,
        cache = newTtlCache<PendingCutoffScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cutoff scores. Empty fields match all. */
data class PendingCutoffScoreQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCutoffScore): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCutoffScore>): MutableList<PendingCutoffScore> {
        val matched = filter(items) { candidate: PendingCutoffScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCutoffScores(store: PendingCutoffScoreStore, query: PendingCutoffScoreQuery): MutableList<PendingCutoffScore> {
    return query.apply(store.list())
}
