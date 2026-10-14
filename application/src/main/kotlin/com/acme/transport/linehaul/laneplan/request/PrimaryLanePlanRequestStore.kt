package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary lane plan requests by code. */
interface PrimaryLanePlanRequestStore {
    fun find(code: String): PrimaryLanePlanRequest?
    fun save(item: PrimaryLanePlanRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLanePlanRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLanePlanRequestStore(
    var items: MutableMap<String, PrimaryLanePlanRequest>,
    var order: MutableList<String>,
) : PrimaryLanePlanRequestStore {
    override fun find(code: String): PrimaryLanePlanRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLanePlanRequest) {
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

    override fun list(): MutableList<PrimaryLanePlanRequest> {
        val out = mutableListOf<PrimaryLanePlanRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLanePlanRequestStore(): PrimaryLanePlanRequestStore {
    return MemoryPrimaryLanePlanRequestStore(
        mutableMapOf<String, PrimaryLanePlanRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLanePlanRequestStore(
    var inner: PrimaryLanePlanRequestStore,
    var cache: TtlCache<PrimaryLanePlanRequest>,
    var hits: Int,
    var misses: Int,
) : PrimaryLanePlanRequestStore {
    override fun find(code: String): PrimaryLanePlanRequest? {
        val hit = (cache.get(code) as PrimaryLanePlanRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLanePlanRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLanePlanRequest> = inner.list()
}

fun newCachedPrimaryLanePlanRequestStore(inner: PrimaryLanePlanRequestStore, ttl: Int, clock: Clock): PrimaryLanePlanRequestStore {
    return CachedPrimaryLanePlanRequestStore(
        inner = inner,
        cache = newTtlCache<PrimaryLanePlanRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary lane plan requests. Empty fields match all. */
data class PrimaryLanePlanRequestQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLanePlanRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryLanePlanRequest>): MutableList<PrimaryLanePlanRequest> {
        val matched = filter(items) { candidate: PrimaryLanePlanRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLanePlanRequests(store: PrimaryLanePlanRequestStore, query: PrimaryLanePlanRequestQuery): MutableList<PrimaryLanePlanRequest> {
    return query.apply(store.list())
}
