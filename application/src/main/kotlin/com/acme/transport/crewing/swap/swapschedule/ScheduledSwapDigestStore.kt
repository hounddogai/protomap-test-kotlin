package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled swap digests by code. */
interface ScheduledSwapDigestStore {
    fun find(code: String): ScheduledSwapDigest?
    fun save(item: ScheduledSwapDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSwapDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSwapDigestStore(
    var items: MutableMap<String, ScheduledSwapDigest>,
    var order: MutableList<String>,
) : ScheduledSwapDigestStore {
    override fun find(code: String): ScheduledSwapDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSwapDigest) {
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

    override fun list(): MutableList<ScheduledSwapDigest> {
        val out = mutableListOf<ScheduledSwapDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSwapDigestStore(): ScheduledSwapDigestStore {
    return MemoryScheduledSwapDigestStore(mutableMapOf<String, ScheduledSwapDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSwapDigestStore(
    var inner: ScheduledSwapDigestStore,
    var cache: TtlCache<ScheduledSwapDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledSwapDigestStore {
    override fun find(code: String): ScheduledSwapDigest? {
        val hit = (cache.get(code) as ScheduledSwapDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSwapDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSwapDigest> = inner.list()
}

fun newCachedScheduledSwapDigestStore(inner: ScheduledSwapDigestStore, ttl: Int, clock: Clock): ScheduledSwapDigestStore {
    return CachedScheduledSwapDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledSwapDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled swap digests. Empty fields match all. */
data class ScheduledSwapDigestQuery(
    var depot: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSwapDigest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledSwapDigest>): MutableList<ScheduledSwapDigest> {
        val matched = filter(items) { candidate: ScheduledSwapDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSwapDigests(store: ScheduledSwapDigestStore, query: ScheduledSwapDigestQuery): MutableList<ScheduledSwapDigest> {
    return query.apply(store.list())
}
