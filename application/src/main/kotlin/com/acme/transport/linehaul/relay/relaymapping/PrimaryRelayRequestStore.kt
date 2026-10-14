package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary relay requests by code. */
interface PrimaryRelayRequestStore {
    fun find(code: String): PrimaryRelayRequest?
    fun save(item: PrimaryRelayRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRelayRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRelayRequestStore(
    var items: MutableMap<String, PrimaryRelayRequest>,
    var order: MutableList<String>,
) : PrimaryRelayRequestStore {
    override fun find(code: String): PrimaryRelayRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRelayRequest) {
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

    override fun list(): MutableList<PrimaryRelayRequest> {
        val out = mutableListOf<PrimaryRelayRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRelayRequestStore(): PrimaryRelayRequestStore {
    return MemoryPrimaryRelayRequestStore(mutableMapOf<String, PrimaryRelayRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRelayRequestStore(
    var inner: PrimaryRelayRequestStore,
    var cache: TtlCache<PrimaryRelayRequest>,
    var hits: Int,
    var misses: Int,
) : PrimaryRelayRequestStore {
    override fun find(code: String): PrimaryRelayRequest? {
        val hit = (cache.get(code) as PrimaryRelayRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRelayRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRelayRequest> = inner.list()
}

fun newCachedPrimaryRelayRequestStore(inner: PrimaryRelayRequestStore, ttl: Int, clock: Clock): PrimaryRelayRequestStore {
    return CachedPrimaryRelayRequestStore(
        inner = inner,
        cache = newTtlCache<PrimaryRelayRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary relay requests. Empty fields match all. */
data class PrimaryRelayRequestQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRelayRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryRelayRequest>): MutableList<PrimaryRelayRequest> {
        val matched = filter(items) { candidate: PrimaryRelayRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRelayRequests(store: PrimaryRelayRequestStore, query: PrimaryRelayRequestQuery): MutableList<PrimaryRelayRequest> {
    return query.apply(store.list())
}
