package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled transit time digests by code. */
interface ScheduledTransitTimeDigestStore {
    fun find(code: String): ScheduledTransitTimeDigest?
    fun save(item: ScheduledTransitTimeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTransitTimeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTransitTimeDigestStore(
    var items: MutableMap<String, ScheduledTransitTimeDigest>,
    var order: MutableList<String>,
) : ScheduledTransitTimeDigestStore {
    override fun find(code: String): ScheduledTransitTimeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTransitTimeDigest) {
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

    override fun list(): MutableList<ScheduledTransitTimeDigest> {
        val out = mutableListOf<ScheduledTransitTimeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTransitTimeDigestStore(): ScheduledTransitTimeDigestStore {
    return MemoryScheduledTransitTimeDigestStore(
        mutableMapOf<String, ScheduledTransitTimeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTransitTimeDigestStore(
    var inner: ScheduledTransitTimeDigestStore,
    var cache: TtlCache<ScheduledTransitTimeDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledTransitTimeDigestStore {
    override fun find(code: String): ScheduledTransitTimeDigest? {
        val hit = (cache.get(code) as ScheduledTransitTimeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTransitTimeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTransitTimeDigest> = inner.list()
}

fun newCachedScheduledTransitTimeDigestStore(inner: ScheduledTransitTimeDigestStore, ttl: Int, clock: Clock): ScheduledTransitTimeDigestStore {
    return CachedScheduledTransitTimeDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledTransitTimeDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled transit time digests. Empty fields match all. */
data class ScheduledTransitTimeDigestQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTransitTimeDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTransitTimeDigest>): MutableList<ScheduledTransitTimeDigest> {
        val matched = filter(items) { candidate: ScheduledTransitTimeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTransitTimeDigests(store: ScheduledTransitTimeDigestStore, query: ScheduledTransitTimeDigestQuery): MutableList<ScheduledTransitTimeDigest> {
    return query.apply(store.list())
}
