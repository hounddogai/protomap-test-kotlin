package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled broker scores by code. */
interface ScheduledBrokerScoreStore {
    fun find(code: String): ScheduledBrokerScore?
    fun save(item: ScheduledBrokerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBrokerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBrokerScoreStore(
    var items: MutableMap<String, ScheduledBrokerScore>,
    var order: MutableList<String>,
) : ScheduledBrokerScoreStore {
    override fun find(code: String): ScheduledBrokerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBrokerScore) {
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

    override fun list(): MutableList<ScheduledBrokerScore> {
        val out = mutableListOf<ScheduledBrokerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBrokerScoreStore(): ScheduledBrokerScoreStore {
    return MemoryScheduledBrokerScoreStore(
        mutableMapOf<String, ScheduledBrokerScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBrokerScoreStore(
    var inner: ScheduledBrokerScoreStore,
    var cache: TtlCache<ScheduledBrokerScore>,
    var hits: Int,
    var misses: Int,
) : ScheduledBrokerScoreStore {
    override fun find(code: String): ScheduledBrokerScore? {
        val hit = (cache.get(code) as ScheduledBrokerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBrokerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBrokerScore> = inner.list()
}

fun newCachedScheduledBrokerScoreStore(inner: ScheduledBrokerScoreStore, ttl: Int, clock: Clock): ScheduledBrokerScoreStore {
    return CachedScheduledBrokerScoreStore(
        inner = inner,
        cache = newTtlCache<ScheduledBrokerScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled broker scores. Empty fields match all. */
data class ScheduledBrokerScoreQuery(
    var unit: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBrokerScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledBrokerScore>): MutableList<ScheduledBrokerScore> {
        val matched = filter(items) { candidate: ScheduledBrokerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBrokerScores(store: ScheduledBrokerScoreStore, query: ScheduledBrokerScoreQuery): MutableList<ScheduledBrokerScore> {
    return query.apply(store.list())
}
