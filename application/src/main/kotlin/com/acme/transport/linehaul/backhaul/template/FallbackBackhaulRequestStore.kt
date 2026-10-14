package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul requests by code. */
interface FallbackBackhaulRequestStore {
    fun find(code: String): FallbackBackhaulRequest?
    fun save(item: FallbackBackhaulRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulRequestStore(
    var items: MutableMap<String, FallbackBackhaulRequest>,
    var order: MutableList<String>,
) : FallbackBackhaulRequestStore {
    override fun find(code: String): FallbackBackhaulRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulRequest) {
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

    override fun list(): MutableList<FallbackBackhaulRequest> {
        val out = mutableListOf<FallbackBackhaulRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulRequestStore(): FallbackBackhaulRequestStore {
    return MemoryFallbackBackhaulRequestStore(
        mutableMapOf<String, FallbackBackhaulRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulRequestStore(
    var inner: FallbackBackhaulRequestStore,
    var cache: TtlCache<FallbackBackhaulRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulRequestStore {
    override fun find(code: String): FallbackBackhaulRequest? {
        val hit = (cache.get(code) as FallbackBackhaulRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulRequest> = inner.list()
}

fun newCachedFallbackBackhaulRequestStore(inner: FallbackBackhaulRequestStore, ttl: Int, clock: Clock): FallbackBackhaulRequestStore {
    return CachedFallbackBackhaulRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul requests. Empty fields match all. */
data class FallbackBackhaulRequestQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulRequest>): MutableList<FallbackBackhaulRequest> {
        val matched = filter(items) { candidate: FallbackBackhaulRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulRequests(store: FallbackBackhaulRequestStore, query: FallbackBackhaulRequestQuery): MutableList<FallbackBackhaulRequest> {
    return query.apply(store.list())
}
