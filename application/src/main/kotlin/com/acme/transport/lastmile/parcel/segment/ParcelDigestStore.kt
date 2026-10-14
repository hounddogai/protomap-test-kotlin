package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel digests by code. */
interface ParcelDigestStore {
    fun find(code: String): ParcelDigest?
    fun save(item: ParcelDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelDigestStore(
    var items: MutableMap<String, ParcelDigest>,
    var order: MutableList<String>,
) : ParcelDigestStore {
    override fun find(code: String): ParcelDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelDigest) {
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

    override fun list(): MutableList<ParcelDigest> {
        val out = mutableListOf<ParcelDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelDigestStore(): ParcelDigestStore {
    return MemoryParcelDigestStore(mutableMapOf<String, ParcelDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelDigestStore(
    var inner: ParcelDigestStore,
    var cache: TtlCache<ParcelDigest>,
    var hits: Int,
    var misses: Int,
) : ParcelDigestStore {
    override fun find(code: String): ParcelDigest? {
        val hit = (cache.get(code) as ParcelDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelDigest> = inner.list()
}

fun newCachedParcelDigestStore(inner: ParcelDigestStore, ttl: Int, clock: Clock): ParcelDigestStore {
    return CachedParcelDigestStore(
        inner = inner,
        cache = newTtlCache<ParcelDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel digests. Empty fields match all. */
data class ParcelDigestQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelDigest>): MutableList<ParcelDigest> {
        val matched = filter(items) { candidate: ParcelDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelDigests(store: ParcelDigestStore, query: ParcelDigestQuery): MutableList<ParcelDigest> {
    return query.apply(store.list())
}
