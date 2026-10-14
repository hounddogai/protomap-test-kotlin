package com.acme.transport.customs.broker.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending broker scores by code. */
interface PendingBrokerScoreStore {
    fun find(code: String): PendingBrokerScore?
    fun save(item: PendingBrokerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBrokerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBrokerScoreStore(
    var items: MutableMap<String, PendingBrokerScore>,
    var order: MutableList<String>,
) : PendingBrokerScoreStore {
    override fun find(code: String): PendingBrokerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBrokerScore) {
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

    override fun list(): MutableList<PendingBrokerScore> {
        val out = mutableListOf<PendingBrokerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBrokerScoreStore(): PendingBrokerScoreStore {
    return MemoryPendingBrokerScoreStore(mutableMapOf<String, PendingBrokerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBrokerScoreStore(
    var inner: PendingBrokerScoreStore,
    var cache: TtlCache<PendingBrokerScore>,
    var hits: Int,
    var misses: Int,
) : PendingBrokerScoreStore {
    override fun find(code: String): PendingBrokerScore? {
        val hit = (cache.get(code) as PendingBrokerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBrokerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBrokerScore> = inner.list()
}

fun newCachedPendingBrokerScoreStore(inner: PendingBrokerScoreStore, ttl: Int, clock: Clock): PendingBrokerScoreStore {
    return CachedPendingBrokerScoreStore(
        inner = inner,
        cache = newTtlCache<PendingBrokerScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending broker scores. Empty fields match all. */
data class PendingBrokerScoreQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBrokerScore): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<PendingBrokerScore>): MutableList<PendingBrokerScore> {
        val matched = filter(items) { candidate: PendingBrokerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBrokerScores(store: PendingBrokerScoreStore, query: PendingBrokerScoreQuery): MutableList<PendingBrokerScore> {
    return query.apply(store.list())
}
