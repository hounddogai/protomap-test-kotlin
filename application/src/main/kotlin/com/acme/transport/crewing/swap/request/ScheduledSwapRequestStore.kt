package com.acme.transport.crewing.swap.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled swap requests by code. */
interface ScheduledSwapRequestStore {
    fun find(code: String): ScheduledSwapRequest?
    fun save(item: ScheduledSwapRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSwapRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSwapRequestStore(
    var items: MutableMap<String, ScheduledSwapRequest>,
    var order: MutableList<String>,
) : ScheduledSwapRequestStore {
    override fun find(code: String): ScheduledSwapRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSwapRequest) {
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

    override fun list(): MutableList<ScheduledSwapRequest> {
        val out = mutableListOf<ScheduledSwapRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSwapRequestStore(): ScheduledSwapRequestStore {
    return MemoryScheduledSwapRequestStore(
        mutableMapOf<String, ScheduledSwapRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSwapRequestStore(
    var inner: ScheduledSwapRequestStore,
    var cache: TtlCache<ScheduledSwapRequest>,
    var hits: Int,
    var misses: Int,
) : ScheduledSwapRequestStore {
    override fun find(code: String): ScheduledSwapRequest? {
        val hit = (cache.get(code) as ScheduledSwapRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSwapRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSwapRequest> = inner.list()
}

fun newCachedScheduledSwapRequestStore(inner: ScheduledSwapRequestStore, ttl: Int, clock: Clock): ScheduledSwapRequestStore {
    return CachedScheduledSwapRequestStore(
        inner = inner,
        cache = newTtlCache<ScheduledSwapRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled swap requests. Empty fields match all. */
data class ScheduledSwapRequestQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSwapRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ScheduledSwapRequest>): MutableList<ScheduledSwapRequest> {
        val matched = filter(items) { candidate: ScheduledSwapRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSwapRequests(store: ScheduledSwapRequestStore, query: ScheduledSwapRequestQuery): MutableList<ScheduledSwapRequest> {
    return query.apply(store.list())
}
