package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching quotas by code. */
interface CoachingQuotaStore {
    fun find(code: String): CoachingQuota?
    fun save(item: CoachingQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingQuotaStore(
    var items: MutableMap<String, CoachingQuota>,
    var order: MutableList<String>,
) : CoachingQuotaStore {
    override fun find(code: String): CoachingQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingQuota) {
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

    override fun list(): MutableList<CoachingQuota> {
        val out = mutableListOf<CoachingQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingQuotaStore(): CoachingQuotaStore {
    return MemoryCoachingQuotaStore(mutableMapOf<String, CoachingQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingQuotaStore(
    var inner: CoachingQuotaStore,
    var cache: TtlCache<CoachingQuota>,
    var hits: Int,
    var misses: Int,
) : CoachingQuotaStore {
    override fun find(code: String): CoachingQuota? {
        val hit = (cache.get(code) as CoachingQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingQuota> = inner.list()
}

fun newCachedCoachingQuotaStore(inner: CoachingQuotaStore, ttl: Int, clock: Clock): CoachingQuotaStore {
    return CachedCoachingQuotaStore(
        inner = inner,
        cache = newTtlCache<CoachingQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching quotas. Empty fields match all. */
data class CoachingQuotaQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingQuota>): MutableList<CoachingQuota> {
        val matched = filter(items) { candidate: CoachingQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingQuotas(store: CoachingQuotaStore, query: CoachingQuotaQuery): MutableList<CoachingQuota> {
    return query.apply(store.list())
}
