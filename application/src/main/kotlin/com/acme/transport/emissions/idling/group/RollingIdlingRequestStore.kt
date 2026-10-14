package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling idling requests by code. */
interface RollingIdlingRequestStore {
    fun find(code: String): RollingIdlingRequest?
    fun save(item: RollingIdlingRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIdlingRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIdlingRequestStore(
    var items: MutableMap<String, RollingIdlingRequest>,
    var order: MutableList<String>,
) : RollingIdlingRequestStore {
    override fun find(code: String): RollingIdlingRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIdlingRequest) {
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

    override fun list(): MutableList<RollingIdlingRequest> {
        val out = mutableListOf<RollingIdlingRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIdlingRequestStore(): RollingIdlingRequestStore {
    return MemoryRollingIdlingRequestStore(
        mutableMapOf<String, RollingIdlingRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIdlingRequestStore(
    var inner: RollingIdlingRequestStore,
    var cache: TtlCache<RollingIdlingRequest>,
    var hits: Int,
    var misses: Int,
) : RollingIdlingRequestStore {
    override fun find(code: String): RollingIdlingRequest? {
        val hit = (cache.get(code) as RollingIdlingRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIdlingRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIdlingRequest> = inner.list()
}

fun newCachedRollingIdlingRequestStore(inner: RollingIdlingRequestStore, ttl: Int, clock: Clock): RollingIdlingRequestStore {
    return CachedRollingIdlingRequestStore(
        inner = inner,
        cache = newTtlCache<RollingIdlingRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling idling requests. Empty fields match all. */
data class RollingIdlingRequestQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIdlingRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIdlingRequest>): MutableList<RollingIdlingRequest> {
        val matched = filter(items) { candidate: RollingIdlingRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIdlingRequests(store: RollingIdlingRequestStore, query: RollingIdlingRequestQuery): MutableList<RollingIdlingRequest> {
    return query.apply(store.list())
}
