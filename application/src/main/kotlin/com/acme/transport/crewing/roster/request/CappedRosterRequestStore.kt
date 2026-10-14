package com.acme.transport.crewing.roster.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped roster requests by code. */
interface CappedRosterRequestStore {
    fun find(code: String): CappedRosterRequest?
    fun save(item: CappedRosterRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRosterRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRosterRequestStore(
    var items: MutableMap<String, CappedRosterRequest>,
    var order: MutableList<String>,
) : CappedRosterRequestStore {
    override fun find(code: String): CappedRosterRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRosterRequest) {
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

    override fun list(): MutableList<CappedRosterRequest> {
        val out = mutableListOf<CappedRosterRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRosterRequestStore(): CappedRosterRequestStore {
    return MemoryCappedRosterRequestStore(mutableMapOf<String, CappedRosterRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRosterRequestStore(
    var inner: CappedRosterRequestStore,
    var cache: TtlCache<CappedRosterRequest>,
    var hits: Int,
    var misses: Int,
) : CappedRosterRequestStore {
    override fun find(code: String): CappedRosterRequest? {
        val hit = (cache.get(code) as CappedRosterRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRosterRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRosterRequest> = inner.list()
}

fun newCachedCappedRosterRequestStore(inner: CappedRosterRequestStore, ttl: Int, clock: Clock): CappedRosterRequestStore {
    return CachedCappedRosterRequestStore(
        inner = inner,
        cache = newTtlCache<CappedRosterRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped roster requests. Empty fields match all. */
data class CappedRosterRequestQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRosterRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedRosterRequest>): MutableList<CappedRosterRequest> {
        val matched = filter(items) { candidate: CappedRosterRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRosterRequests(store: CappedRosterRequestStore, query: CappedRosterRequestQuery): MutableList<CappedRosterRequest> {
    return query.apply(store.list())
}
