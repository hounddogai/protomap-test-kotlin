package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff requests by code. */
interface CutoffRequestStore {
    fun find(code: String): CutoffRequest?
    fun save(item: CutoffRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffRequestStore(
    var items: MutableMap<String, CutoffRequest>,
    var order: MutableList<String>,
) : CutoffRequestStore {
    override fun find(code: String): CutoffRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffRequest) {
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

    override fun list(): MutableList<CutoffRequest> {
        val out = mutableListOf<CutoffRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffRequestStore(): CutoffRequestStore {
    return MemoryCutoffRequestStore(mutableMapOf<String, CutoffRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffRequestStore(
    var inner: CutoffRequestStore,
    var cache: TtlCache<CutoffRequest>,
    var hits: Int,
    var misses: Int,
) : CutoffRequestStore {
    override fun find(code: String): CutoffRequest? {
        val hit = (cache.get(code) as CutoffRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffRequest> = inner.list()
}

fun newCachedCutoffRequestStore(inner: CutoffRequestStore, ttl: Int, clock: Clock): CutoffRequestStore {
    return CachedCutoffRequestStore(
        inner = inner,
        cache = newTtlCache<CutoffRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff requests. Empty fields match all. */
data class CutoffRequestQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffRequest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffRequest>): MutableList<CutoffRequest> {
        val matched = filter(items) { candidate: CutoffRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffRequests(store: CutoffRequestStore, query: CutoffRequestQuery): MutableList<CutoffRequest> {
    return query.apply(store.list())
}
