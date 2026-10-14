package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker digests by code. */
interface LockerDigestStore {
    fun find(code: String): LockerDigest?
    fun save(item: LockerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerDigestStore(
    var items: MutableMap<String, LockerDigest>,
    var order: MutableList<String>,
) : LockerDigestStore {
    override fun find(code: String): LockerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerDigest) {
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

    override fun list(): MutableList<LockerDigest> {
        val out = mutableListOf<LockerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerDigestStore(): LockerDigestStore {
    return MemoryLockerDigestStore(mutableMapOf<String, LockerDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerDigestStore(
    var inner: LockerDigestStore,
    var cache: TtlCache<LockerDigest>,
    var hits: Int,
    var misses: Int,
) : LockerDigestStore {
    override fun find(code: String): LockerDigest? {
        val hit = (cache.get(code) as LockerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerDigest> = inner.list()
}

fun newCachedLockerDigestStore(inner: LockerDigestStore, ttl: Int, clock: Clock): LockerDigestStore {
    return CachedLockerDigestStore(
        inner = inner,
        cache = newTtlCache<LockerDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker digests. Empty fields match all. */
data class LockerDigestQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerDigest>): MutableList<LockerDigest> {
        val matched = filter(items) { candidate: LockerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerDigests(store: LockerDigestStore, query: LockerDigestQuery): MutableList<LockerDigest> {
    return query.apply(store.list())
}
