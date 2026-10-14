package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal relay requests by code. */
interface SeasonalRelayRequestStore {
    fun find(code: String): SeasonalRelayRequest?
    fun save(item: SeasonalRelayRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRelayRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRelayRequestStore(
    var items: MutableMap<String, SeasonalRelayRequest>,
    var order: MutableList<String>,
) : SeasonalRelayRequestStore {
    override fun find(code: String): SeasonalRelayRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRelayRequest) {
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

    override fun list(): MutableList<SeasonalRelayRequest> {
        val out = mutableListOf<SeasonalRelayRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRelayRequestStore(): SeasonalRelayRequestStore {
    return MemorySeasonalRelayRequestStore(
        mutableMapOf<String, SeasonalRelayRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRelayRequestStore(
    var inner: SeasonalRelayRequestStore,
    var cache: TtlCache<SeasonalRelayRequest>,
    var hits: Int,
    var misses: Int,
) : SeasonalRelayRequestStore {
    override fun find(code: String): SeasonalRelayRequest? {
        val hit = (cache.get(code) as SeasonalRelayRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRelayRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRelayRequest> = inner.list()
}

fun newCachedSeasonalRelayRequestStore(inner: SeasonalRelayRequestStore, ttl: Int, clock: Clock): SeasonalRelayRequestStore {
    return CachedSeasonalRelayRequestStore(
        inner = inner,
        cache = newTtlCache<SeasonalRelayRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal relay requests. Empty fields match all. */
data class SeasonalRelayRequestQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRelayRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<SeasonalRelayRequest>): MutableList<SeasonalRelayRequest> {
        val matched = filter(items) { candidate: SeasonalRelayRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRelayRequests(store: SeasonalRelayRequestStore, query: SeasonalRelayRequestQuery): MutableList<SeasonalRelayRequest> {
    return query.apply(store.list())
}
