package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard digests by code. */
interface TrailerYardDigestStore {
    fun find(code: String): TrailerYardDigest?
    fun save(item: TrailerYardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardDigestStore(
    var items: MutableMap<String, TrailerYardDigest>,
    var order: MutableList<String>,
) : TrailerYardDigestStore {
    override fun find(code: String): TrailerYardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardDigest) {
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

    override fun list(): MutableList<TrailerYardDigest> {
        val out = mutableListOf<TrailerYardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardDigestStore(): TrailerYardDigestStore {
    return MemoryTrailerYardDigestStore(mutableMapOf<String, TrailerYardDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardDigestStore(
    var inner: TrailerYardDigestStore,
    var cache: TtlCache<TrailerYardDigest>,
    var hits: Int,
    var misses: Int,
) : TrailerYardDigestStore {
    override fun find(code: String): TrailerYardDigest? {
        val hit = (cache.get(code) as TrailerYardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardDigest> = inner.list()
}

fun newCachedTrailerYardDigestStore(inner: TrailerYardDigestStore, ttl: Int, clock: Clock): TrailerYardDigestStore {
    return CachedTrailerYardDigestStore(
        inner = inner,
        cache = newTtlCache<TrailerYardDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard digests. Empty fields match all. */
data class TrailerYardDigestQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardDigest>): MutableList<TrailerYardDigest> {
        val matched = filter(items) { candidate: TrailerYardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardDigests(store: TrailerYardDigestStore, query: TrailerYardDigestQuery): MutableList<TrailerYardDigest> {
    return query.apply(store.list())
}
