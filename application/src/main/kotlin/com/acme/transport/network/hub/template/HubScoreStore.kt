package com.acme.transport.network.hub.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub scores by code. */
interface HubScoreStore {
    fun find(code: String): HubScore?
    fun save(item: HubScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubScoreStore(
    var items: MutableMap<String, HubScore>,
    var order: MutableList<String>,
) : HubScoreStore {
    override fun find(code: String): HubScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubScore) {
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

    override fun list(): MutableList<HubScore> {
        val out = mutableListOf<HubScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubScoreStore(): HubScoreStore {
    return MemoryHubScoreStore(mutableMapOf<String, HubScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubScoreStore(
    var inner: HubScoreStore,
    var cache: TtlCache<HubScore>,
    var hits: Int,
    var misses: Int,
) : HubScoreStore {
    override fun find(code: String): HubScore? {
        val hit = (cache.get(code) as HubScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubScore> = inner.list()
}

fun newCachedHubScoreStore(inner: HubScoreStore, ttl: Int, clock: Clock): HubScoreStore {
    return CachedHubScoreStore(
        inner = inner,
        cache = newTtlCache<HubScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub scores. Empty fields match all. */
data class HubScoreQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubScore): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubScore>): MutableList<HubScore> {
        val matched = filter(items) { candidate: HubScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubScores(store: HubScoreStore, query: HubScoreQuery): MutableList<HubScore> = query.apply(store.list())
