package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold digests by code. */
interface HoldDigestStore {
    fun find(code: String): HoldDigest?
    fun save(item: HoldDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldDigestStore(
    var items: MutableMap<String, HoldDigest>,
    var order: MutableList<String>,
) : HoldDigestStore {
    override fun find(code: String): HoldDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldDigest) {
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

    override fun list(): MutableList<HoldDigest> {
        val out = mutableListOf<HoldDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldDigestStore(): HoldDigestStore {
    return MemoryHoldDigestStore(mutableMapOf<String, HoldDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldDigestStore(
    var inner: HoldDigestStore,
    var cache: TtlCache<HoldDigest>,
    var hits: Int,
    var misses: Int,
) : HoldDigestStore {
    override fun find(code: String): HoldDigest? {
        val hit = (cache.get(code) as HoldDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldDigest> = inner.list()
}

fun newCachedHoldDigestStore(inner: HoldDigestStore, ttl: Int, clock: Clock): HoldDigestStore {
    return CachedHoldDigestStore(
        inner = inner,
        cache = newTtlCache<HoldDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold digests. Empty fields match all. */
data class HoldDigestQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<HoldDigest>): MutableList<HoldDigest> {
        val matched = filter(items) { candidate: HoldDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldDigests(store: HoldDigestStore, query: HoldDigestQuery): MutableList<HoldDigest> {
    return query.apply(store.list())
}
