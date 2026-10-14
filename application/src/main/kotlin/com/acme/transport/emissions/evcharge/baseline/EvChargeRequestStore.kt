package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge requests by code. */
interface EvChargeRequestStore {
    fun find(code: String): EvChargeRequest?
    fun save(item: EvChargeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeRequestStore(
    var items: MutableMap<String, EvChargeRequest>,
    var order: MutableList<String>,
) : EvChargeRequestStore {
    override fun find(code: String): EvChargeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeRequest) {
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

    override fun list(): MutableList<EvChargeRequest> {
        val out = mutableListOf<EvChargeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeRequestStore(): EvChargeRequestStore {
    return MemoryEvChargeRequestStore(mutableMapOf<String, EvChargeRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeRequestStore(
    var inner: EvChargeRequestStore,
    var cache: TtlCache<EvChargeRequest>,
    var hits: Int,
    var misses: Int,
) : EvChargeRequestStore {
    override fun find(code: String): EvChargeRequest? {
        val hit = (cache.get(code) as EvChargeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeRequest> = inner.list()
}

fun newCachedEvChargeRequestStore(inner: EvChargeRequestStore, ttl: Int, clock: Clock): EvChargeRequestStore {
    return CachedEvChargeRequestStore(
        inner = inner,
        cache = newTtlCache<EvChargeRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge requests. Empty fields match all. */
data class EvChargeRequestQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<EvChargeRequest>): MutableList<EvChargeRequest> {
        val matched = filter(items) { candidate: EvChargeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeRequests(store: EvChargeRequestStore, query: EvChargeRequestQuery): MutableList<EvChargeRequest> {
    return query.apply(store.list())
}
