package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary parcel digests by code. */
interface PrimaryParcelDigestStore {
    fun find(code: String): PrimaryParcelDigest?
    fun save(item: PrimaryParcelDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryParcelDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryParcelDigestStore(
    var items: MutableMap<String, PrimaryParcelDigest>,
    var order: MutableList<String>,
) : PrimaryParcelDigestStore {
    override fun find(code: String): PrimaryParcelDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryParcelDigest) {
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

    override fun list(): MutableList<PrimaryParcelDigest> {
        val out = mutableListOf<PrimaryParcelDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryParcelDigestStore(): PrimaryParcelDigestStore {
    return MemoryPrimaryParcelDigestStore(mutableMapOf<String, PrimaryParcelDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryParcelDigestStore(
    var inner: PrimaryParcelDigestStore,
    var cache: TtlCache<PrimaryParcelDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryParcelDigestStore {
    override fun find(code: String): PrimaryParcelDigest? {
        val hit = (cache.get(code) as PrimaryParcelDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryParcelDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryParcelDigest> = inner.list()
}

fun newCachedPrimaryParcelDigestStore(inner: PrimaryParcelDigestStore, ttl: Int, clock: Clock): PrimaryParcelDigestStore {
    return CachedPrimaryParcelDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryParcelDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary parcel digests. Empty fields match all. */
data class PrimaryParcelDigestQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryParcelDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryParcelDigest>): MutableList<PrimaryParcelDigest> {
        val matched = filter(items) { candidate: PrimaryParcelDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryParcelDigests(store: PrimaryParcelDigestStore, query: PrimaryParcelDigestQuery): MutableList<PrimaryParcelDigest> {
    return query.apply(store.list())
}
