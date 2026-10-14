package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending tender scores by code. */
interface PendingTenderScoreStore {
    fun find(code: String): PendingTenderScore?
    fun save(item: PendingTenderScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTenderScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTenderScoreStore(
    var items: MutableMap<String, PendingTenderScore>,
    var order: MutableList<String>,
) : PendingTenderScoreStore {
    override fun find(code: String): PendingTenderScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTenderScore) {
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

    override fun list(): MutableList<PendingTenderScore> {
        val out = mutableListOf<PendingTenderScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTenderScoreStore(): PendingTenderScoreStore {
    return MemoryPendingTenderScoreStore(mutableMapOf<String, PendingTenderScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTenderScoreStore(
    var inner: PendingTenderScoreStore,
    var cache: TtlCache<PendingTenderScore>,
    var hits: Int,
    var misses: Int,
) : PendingTenderScoreStore {
    override fun find(code: String): PendingTenderScore? {
        val hit = (cache.get(code) as PendingTenderScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTenderScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTenderScore> = inner.list()
}

fun newCachedPendingTenderScoreStore(inner: PendingTenderScoreStore, ttl: Int, clock: Clock): PendingTenderScoreStore {
    return CachedPendingTenderScoreStore(
        inner = inner,
        cache = newTtlCache<PendingTenderScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tender scores. Empty fields match all. */
data class PendingTenderScoreQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTenderScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTenderScore>): MutableList<PendingTenderScore> {
        val matched = filter(items) { candidate: PendingTenderScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTenderScores(store: PendingTenderScoreStore, query: PendingTenderScoreQuery): MutableList<PendingTenderScore> {
    return query.apply(store.list())
}
