package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered broker scores by code. */
interface TieredBrokerScoreStore {
    fun find(code: String): TieredBrokerScore?
    fun save(item: TieredBrokerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBrokerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBrokerScoreStore(
    var items: MutableMap<String, TieredBrokerScore>,
    var order: MutableList<String>,
) : TieredBrokerScoreStore {
    override fun find(code: String): TieredBrokerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBrokerScore) {
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

    override fun list(): MutableList<TieredBrokerScore> {
        val out = mutableListOf<TieredBrokerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBrokerScoreStore(): TieredBrokerScoreStore {
    return MemoryTieredBrokerScoreStore(mutableMapOf<String, TieredBrokerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBrokerScoreStore(
    var inner: TieredBrokerScoreStore,
    var cache: TtlCache<TieredBrokerScore>,
    var hits: Int,
    var misses: Int,
) : TieredBrokerScoreStore {
    override fun find(code: String): TieredBrokerScore? {
        val hit = (cache.get(code) as TieredBrokerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBrokerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBrokerScore> = inner.list()
}

fun newCachedTieredBrokerScoreStore(inner: TieredBrokerScoreStore, ttl: Int, clock: Clock): TieredBrokerScoreStore {
    return CachedTieredBrokerScoreStore(
        inner = inner,
        cache = newTtlCache<TieredBrokerScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered broker scores. Empty fields match all. */
data class TieredBrokerScoreQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBrokerScore): Boolean {
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

    fun apply(items: MutableList<TieredBrokerScore>): MutableList<TieredBrokerScore> {
        val matched = filter(items) { candidate: TieredBrokerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBrokerScores(store: TieredBrokerScoreStore, query: TieredBrokerScoreQuery): MutableList<TieredBrokerScore> {
    return query.apply(store.list())
}
