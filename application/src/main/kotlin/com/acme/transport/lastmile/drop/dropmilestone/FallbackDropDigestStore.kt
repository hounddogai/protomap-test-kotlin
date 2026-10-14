package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop digests by code. */
interface FallbackDropDigestStore {
    fun find(code: String): FallbackDropDigest?
    fun save(item: FallbackDropDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropDigestStore(
    var items: MutableMap<String, FallbackDropDigest>,
    var order: MutableList<String>,
) : FallbackDropDigestStore {
    override fun find(code: String): FallbackDropDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropDigest) {
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

    override fun list(): MutableList<FallbackDropDigest> {
        val out = mutableListOf<FallbackDropDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropDigestStore(): FallbackDropDigestStore {
    return MemoryFallbackDropDigestStore(mutableMapOf<String, FallbackDropDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropDigestStore(
    var inner: FallbackDropDigestStore,
    var cache: TtlCache<FallbackDropDigest>,
    var hits: Int,
    var misses: Int,
) : FallbackDropDigestStore {
    override fun find(code: String): FallbackDropDigest? {
        val hit = (cache.get(code) as FallbackDropDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropDigest> = inner.list()
}

fun newCachedFallbackDropDigestStore(inner: FallbackDropDigestStore, ttl: Int, clock: Clock): FallbackDropDigestStore {
    return CachedFallbackDropDigestStore(
        inner = inner,
        cache = newTtlCache<FallbackDropDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop digests. Empty fields match all. */
data class FallbackDropDigestQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackDropDigest>): MutableList<FallbackDropDigest> {
        val matched = filter(items) { candidate: FallbackDropDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropDigests(store: FallbackDropDigestStore, query: FallbackDropDigestQuery): MutableList<FallbackDropDigest> {
    return query.apply(store.list())
}
