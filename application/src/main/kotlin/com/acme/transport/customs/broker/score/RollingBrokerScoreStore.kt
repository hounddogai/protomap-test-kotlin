package com.acme.transport.customs.broker.score

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling broker scores by code. */
interface RollingBrokerScoreStore {
    fun find(code: String): RollingBrokerScore?
    fun save(item: RollingBrokerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBrokerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBrokerScoreStore(
    var items: MutableMap<String, RollingBrokerScore>,
    var order: MutableList<String>,
) : RollingBrokerScoreStore {
    override fun find(code: String): RollingBrokerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBrokerScore) {
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

    override fun list(): MutableList<RollingBrokerScore> {
        val out = mutableListOf<RollingBrokerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBrokerScoreStore(): RollingBrokerScoreStore {
    return MemoryRollingBrokerScoreStore(mutableMapOf<String, RollingBrokerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBrokerScoreStore(
    var inner: RollingBrokerScoreStore,
    var cache: TtlCache<RollingBrokerScore>,
    var hits: Int,
    var misses: Int,
) : RollingBrokerScoreStore {
    override fun find(code: String): RollingBrokerScore? {
        val hit = (cache.get(code) as RollingBrokerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBrokerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBrokerScore> = inner.list()
}

fun newCachedRollingBrokerScoreStore(inner: RollingBrokerScoreStore, ttl: Int, clock: Clock): RollingBrokerScoreStore {
    return CachedRollingBrokerScoreStore(
        inner = inner,
        cache = newTtlCache<RollingBrokerScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling broker scores. Empty fields match all. */
data class RollingBrokerScoreQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBrokerScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBrokerScore>): MutableList<RollingBrokerScore> {
        val matched = filter(items) { candidate: RollingBrokerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBrokerScores(store: RollingBrokerScoreStore, query: RollingBrokerScoreQuery): MutableList<RollingBrokerScore> {
    return query.apply(store.list())
}
