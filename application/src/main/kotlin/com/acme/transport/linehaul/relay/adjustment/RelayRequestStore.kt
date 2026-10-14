package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay requests by code. */
interface RelayRequestStore {
    fun find(code: String): RelayRequest?
    fun save(item: RelayRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayRequestStore(
    var items: MutableMap<String, RelayRequest>,
    var order: MutableList<String>,
) : RelayRequestStore {
    override fun find(code: String): RelayRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayRequest) {
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

    override fun list(): MutableList<RelayRequest> {
        val out = mutableListOf<RelayRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayRequestStore(): RelayRequestStore {
    return MemoryRelayRequestStore(mutableMapOf<String, RelayRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayRequestStore(
    var inner: RelayRequestStore,
    var cache: TtlCache<RelayRequest>,
    var hits: Int,
    var misses: Int,
) : RelayRequestStore {
    override fun find(code: String): RelayRequest? {
        val hit = (cache.get(code) as RelayRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayRequest> = inner.list()
}

fun newCachedRelayRequestStore(inner: RelayRequestStore, ttl: Int, clock: Clock): RelayRequestStore {
    return CachedRelayRequestStore(
        inner = inner,
        cache = newTtlCache<RelayRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay requests. Empty fields match all. */
data class RelayRequestQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayRequest>): MutableList<RelayRequest> {
        val matched = filter(items) { candidate: RelayRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayRequests(store: RelayRequestStore, query: RelayRequestQuery): MutableList<RelayRequest> {
    return query.apply(store.list())
}
