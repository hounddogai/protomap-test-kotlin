package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback scorecard quotas by code. */
interface FallbackScorecardQuotaStore {
    fun find(code: String): FallbackScorecardQuota?
    fun save(item: FallbackScorecardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackScorecardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackScorecardQuotaStore(
    var items: MutableMap<String, FallbackScorecardQuota>,
    var order: MutableList<String>,
) : FallbackScorecardQuotaStore {
    override fun find(code: String): FallbackScorecardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackScorecardQuota) {
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

    override fun list(): MutableList<FallbackScorecardQuota> {
        val out = mutableListOf<FallbackScorecardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackScorecardQuotaStore(): FallbackScorecardQuotaStore {
    return MemoryFallbackScorecardQuotaStore(
        mutableMapOf<String, FallbackScorecardQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackScorecardQuotaStore(
    var inner: FallbackScorecardQuotaStore,
    var cache: TtlCache<FallbackScorecardQuota>,
    var hits: Int,
    var misses: Int,
) : FallbackScorecardQuotaStore {
    override fun find(code: String): FallbackScorecardQuota? {
        val hit = (cache.get(code) as FallbackScorecardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackScorecardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackScorecardQuota> = inner.list()
}

fun newCachedFallbackScorecardQuotaStore(inner: FallbackScorecardQuotaStore, ttl: Int, clock: Clock): FallbackScorecardQuotaStore {
    return CachedFallbackScorecardQuotaStore(
        inner = inner,
        cache = newTtlCache<FallbackScorecardQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback scorecard quotas. Empty fields match all. */
data class FallbackScorecardQuotaQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackScorecardQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<FallbackScorecardQuota>): MutableList<FallbackScorecardQuota> {
        val matched = filter(items) { candidate: FallbackScorecardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackScorecardQuotas(store: FallbackScorecardQuotaStore, query: FallbackScorecardQuotaQuery): MutableList<FallbackScorecardQuota> {
    return query.apply(store.list())
}
