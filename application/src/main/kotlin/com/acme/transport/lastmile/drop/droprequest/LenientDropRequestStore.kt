package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient drop requests by code. */
interface LenientDropRequestStore {
    fun find(code: String): LenientDropRequest?
    fun save(item: LenientDropRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDropRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDropRequestStore(
    var items: MutableMap<String, LenientDropRequest>,
    var order: MutableList<String>,
) : LenientDropRequestStore {
    override fun find(code: String): LenientDropRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDropRequest) {
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

    override fun list(): MutableList<LenientDropRequest> {
        val out = mutableListOf<LenientDropRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDropRequestStore(): LenientDropRequestStore {
    return MemoryLenientDropRequestStore(mutableMapOf<String, LenientDropRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDropRequestStore(
    var inner: LenientDropRequestStore,
    var cache: TtlCache<LenientDropRequest>,
    var hits: Int,
    var misses: Int,
) : LenientDropRequestStore {
    override fun find(code: String): LenientDropRequest? {
        val hit = (cache.get(code) as LenientDropRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDropRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDropRequest> = inner.list()
}

fun newCachedLenientDropRequestStore(inner: LenientDropRequestStore, ttl: Int, clock: Clock): LenientDropRequestStore {
    return CachedLenientDropRequestStore(
        inner = inner,
        cache = newTtlCache<LenientDropRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient drop requests. Empty fields match all. */
data class LenientDropRequestQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDropRequest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LenientDropRequest>): MutableList<LenientDropRequest> {
        val matched = filter(items) { candidate: LenientDropRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDropRequests(store: LenientDropRequestStore, query: LenientDropRequestQuery): MutableList<LenientDropRequest> {
    return query.apply(store.list())
}
