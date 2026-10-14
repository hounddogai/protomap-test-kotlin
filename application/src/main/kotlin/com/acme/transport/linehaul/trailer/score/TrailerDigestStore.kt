package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer digests by code. */
interface TrailerDigestStore {
    fun find(code: String): TrailerDigest?
    fun save(item: TrailerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerDigestStore(
    var items: MutableMap<String, TrailerDigest>,
    var order: MutableList<String>,
) : TrailerDigestStore {
    override fun find(code: String): TrailerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerDigest) {
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

    override fun list(): MutableList<TrailerDigest> {
        val out = mutableListOf<TrailerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerDigestStore(): TrailerDigestStore {
    return MemoryTrailerDigestStore(mutableMapOf<String, TrailerDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerDigestStore(
    var inner: TrailerDigestStore,
    var cache: TtlCache<TrailerDigest>,
    var hits: Int,
    var misses: Int,
) : TrailerDigestStore {
    override fun find(code: String): TrailerDigest? {
        val hit = (cache.get(code) as TrailerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerDigest> = inner.list()
}

fun newCachedTrailerDigestStore(inner: TrailerDigestStore, ttl: Int, clock: Clock): TrailerDigestStore {
    return CachedTrailerDigestStore(
        inner = inner,
        cache = newTtlCache<TrailerDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer digests. Empty fields match all. */
data class TrailerDigestQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TrailerDigest>): MutableList<TrailerDigest> {
        val matched = filter(items) { candidate: TrailerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerDigests(store: TrailerDigestStore, query: TrailerDigestQuery): MutableList<TrailerDigest> {
    return query.apply(store.list())
}
