package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rest break digests by code. */
interface ScheduledRestBreakDigestStore {
    fun find(code: String): ScheduledRestBreakDigest?
    fun save(item: ScheduledRestBreakDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRestBreakDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRestBreakDigestStore(
    var items: MutableMap<String, ScheduledRestBreakDigest>,
    var order: MutableList<String>,
) : ScheduledRestBreakDigestStore {
    override fun find(code: String): ScheduledRestBreakDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRestBreakDigest) {
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

    override fun list(): MutableList<ScheduledRestBreakDigest> {
        val out = mutableListOf<ScheduledRestBreakDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRestBreakDigestStore(): ScheduledRestBreakDigestStore {
    return MemoryScheduledRestBreakDigestStore(
        mutableMapOf<String, ScheduledRestBreakDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRestBreakDigestStore(
    var inner: ScheduledRestBreakDigestStore,
    var cache: TtlCache<ScheduledRestBreakDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledRestBreakDigestStore {
    override fun find(code: String): ScheduledRestBreakDigest? {
        val hit = (cache.get(code) as ScheduledRestBreakDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRestBreakDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRestBreakDigest> = inner.list()
}

fun newCachedScheduledRestBreakDigestStore(inner: ScheduledRestBreakDigestStore, ttl: Int, clock: Clock): ScheduledRestBreakDigestStore {
    return CachedScheduledRestBreakDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledRestBreakDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rest break digests. Empty fields match all. */
data class ScheduledRestBreakDigestQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRestBreakDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRestBreakDigest>): MutableList<ScheduledRestBreakDigest> {
        val matched = filter(items) { candidate: ScheduledRestBreakDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRestBreakDigests(store: ScheduledRestBreakDigestStore, query: ScheduledRestBreakDigestQuery): MutableList<ScheduledRestBreakDigest> {
    return query.apply(store.list())
}
