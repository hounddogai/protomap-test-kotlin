package com.acme.transport.customs.broker.score

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker scores by code. */
interface BrokerScoreStore {
    fun find(code: String): BrokerScore?
    fun save(item: BrokerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerScoreStore(
    var items: MutableMap<String, BrokerScore>,
    var order: MutableList<String>,
) : BrokerScoreStore {
    override fun find(code: String): BrokerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerScore) {
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

    override fun list(): MutableList<BrokerScore> {
        val out = mutableListOf<BrokerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerScoreStore(): BrokerScoreStore {
    return MemoryBrokerScoreStore(mutableMapOf<String, BrokerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerScoreStore(
    var inner: BrokerScoreStore,
    var cache: TtlCache<BrokerScore>,
    var hits: Int,
    var misses: Int,
) : BrokerScoreStore {
    override fun find(code: String): BrokerScore? {
        val hit = (cache.get(code) as BrokerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerScore> = inner.list()
}

fun newCachedBrokerScoreStore(inner: BrokerScoreStore, ttl: Int, clock: Clock): BrokerScoreStore {
    return CachedBrokerScoreStore(
        inner = inner,
        cache = newTtlCache<BrokerScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker scores. Empty fields match all. */
data class BrokerScoreQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerScore): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerScore>): MutableList<BrokerScore> {
        val matched = filter(items) { candidate: BrokerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerScores(store: BrokerScoreStore, query: BrokerScoreQuery): MutableList<BrokerScore> {
    return query.apply(store.list())
}
