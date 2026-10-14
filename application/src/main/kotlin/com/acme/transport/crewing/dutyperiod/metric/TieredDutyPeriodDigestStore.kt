package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered duty period digests by code. */
interface TieredDutyPeriodDigestStore {
    fun find(code: String): TieredDutyPeriodDigest?
    fun save(item: TieredDutyPeriodDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDutyPeriodDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDutyPeriodDigestStore(
    var items: MutableMap<String, TieredDutyPeriodDigest>,
    var order: MutableList<String>,
) : TieredDutyPeriodDigestStore {
    override fun find(code: String): TieredDutyPeriodDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDutyPeriodDigest) {
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

    override fun list(): MutableList<TieredDutyPeriodDigest> {
        val out = mutableListOf<TieredDutyPeriodDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDutyPeriodDigestStore(): TieredDutyPeriodDigestStore {
    return MemoryTieredDutyPeriodDigestStore(
        mutableMapOf<String, TieredDutyPeriodDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDutyPeriodDigestStore(
    var inner: TieredDutyPeriodDigestStore,
    var cache: TtlCache<TieredDutyPeriodDigest>,
    var hits: Int,
    var misses: Int,
) : TieredDutyPeriodDigestStore {
    override fun find(code: String): TieredDutyPeriodDigest? {
        val hit = (cache.get(code) as TieredDutyPeriodDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDutyPeriodDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDutyPeriodDigest> = inner.list()
}

fun newCachedTieredDutyPeriodDigestStore(inner: TieredDutyPeriodDigestStore, ttl: Int, clock: Clock): TieredDutyPeriodDigestStore {
    return CachedTieredDutyPeriodDigestStore(
        inner = inner,
        cache = newTtlCache<TieredDutyPeriodDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered duty period digests. Empty fields match all. */
data class TieredDutyPeriodDigestQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDutyPeriodDigest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDutyPeriodDigest>): MutableList<TieredDutyPeriodDigest> {
        val matched = filter(items) { candidate: TieredDutyPeriodDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDutyPeriodDigests(store: TieredDutyPeriodDigestStore, query: TieredDutyPeriodDigestQuery): MutableList<TieredDutyPeriodDigest> {
    return query.apply(store.list())
}
