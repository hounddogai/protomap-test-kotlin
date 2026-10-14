package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance digests by code. */
interface ClearanceDigestStore {
    fun find(code: String): ClearanceDigest?
    fun save(item: ClearanceDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceDigestStore(
    var items: MutableMap<String, ClearanceDigest>,
    var order: MutableList<String>,
) : ClearanceDigestStore {
    override fun find(code: String): ClearanceDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceDigest) {
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

    override fun list(): MutableList<ClearanceDigest> {
        val out = mutableListOf<ClearanceDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceDigestStore(): ClearanceDigestStore {
    return MemoryClearanceDigestStore(mutableMapOf<String, ClearanceDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceDigestStore(
    var inner: ClearanceDigestStore,
    var cache: TtlCache<ClearanceDigest>,
    var hits: Int,
    var misses: Int,
) : ClearanceDigestStore {
    override fun find(code: String): ClearanceDigest? {
        val hit = (cache.get(code) as ClearanceDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceDigest> = inner.list()
}

fun newCachedClearanceDigestStore(inner: ClearanceDigestStore, ttl: Int, clock: Clock): ClearanceDigestStore {
    return CachedClearanceDigestStore(
        inner = inner,
        cache = newTtlCache<ClearanceDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance digests. Empty fields match all. */
data class ClearanceDigestQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceDigest>): MutableList<ClearanceDigest> {
        val matched = filter(items) { candidate: ClearanceDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceDigests(store: ClearanceDigestStore, query: ClearanceDigestQuery): MutableList<ClearanceDigest> {
    return query.apply(store.list())
}
