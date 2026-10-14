package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient backhaul requests by code. */
interface LenientBackhaulRequestStore {
    fun find(code: String): LenientBackhaulRequest?
    fun save(item: LenientBackhaulRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBackhaulRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBackhaulRequestStore(
    var items: MutableMap<String, LenientBackhaulRequest>,
    var order: MutableList<String>,
) : LenientBackhaulRequestStore {
    override fun find(code: String): LenientBackhaulRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBackhaulRequest) {
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

    override fun list(): MutableList<LenientBackhaulRequest> {
        val out = mutableListOf<LenientBackhaulRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBackhaulRequestStore(): LenientBackhaulRequestStore {
    return MemoryLenientBackhaulRequestStore(
        mutableMapOf<String, LenientBackhaulRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBackhaulRequestStore(
    var inner: LenientBackhaulRequestStore,
    var cache: TtlCache<LenientBackhaulRequest>,
    var hits: Int,
    var misses: Int,
) : LenientBackhaulRequestStore {
    override fun find(code: String): LenientBackhaulRequest? {
        val hit = (cache.get(code) as LenientBackhaulRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBackhaulRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBackhaulRequest> = inner.list()
}

fun newCachedLenientBackhaulRequestStore(inner: LenientBackhaulRequestStore, ttl: Int, clock: Clock): LenientBackhaulRequestStore {
    return CachedLenientBackhaulRequestStore(
        inner = inner,
        cache = newTtlCache<LenientBackhaulRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient backhaul requests. Empty fields match all. */
data class LenientBackhaulRequestQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBackhaulRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientBackhaulRequest>): MutableList<LenientBackhaulRequest> {
        val matched = filter(items) { candidate: LenientBackhaulRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBackhaulRequests(store: LenientBackhaulRequestStore, query: LenientBackhaulRequestQuery): MutableList<LenientBackhaulRequest> {
    return query.apply(store.list())
}
