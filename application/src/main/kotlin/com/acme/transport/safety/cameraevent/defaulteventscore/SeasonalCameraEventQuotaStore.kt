package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal camera event quotas by code. */
interface SeasonalCameraEventQuotaStore {
    fun find(code: String): SeasonalCameraEventQuota?
    fun save(item: SeasonalCameraEventQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCameraEventQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCameraEventQuotaStore(
    var items: MutableMap<String, SeasonalCameraEventQuota>,
    var order: MutableList<String>,
) : SeasonalCameraEventQuotaStore {
    override fun find(code: String): SeasonalCameraEventQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCameraEventQuota) {
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

    override fun list(): MutableList<SeasonalCameraEventQuota> {
        val out = mutableListOf<SeasonalCameraEventQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCameraEventQuotaStore(): SeasonalCameraEventQuotaStore {
    return MemorySeasonalCameraEventQuotaStore(
        mutableMapOf<String, SeasonalCameraEventQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCameraEventQuotaStore(
    var inner: SeasonalCameraEventQuotaStore,
    var cache: TtlCache<SeasonalCameraEventQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalCameraEventQuotaStore {
    override fun find(code: String): SeasonalCameraEventQuota? {
        val hit = (cache.get(code) as SeasonalCameraEventQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCameraEventQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCameraEventQuota> = inner.list()
}

fun newCachedSeasonalCameraEventQuotaStore(inner: SeasonalCameraEventQuotaStore, ttl: Int, clock: Clock): SeasonalCameraEventQuotaStore {
    return CachedSeasonalCameraEventQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalCameraEventQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal camera event quotas. Empty fields match all. */
data class SeasonalCameraEventQuotaQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCameraEventQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCameraEventQuota>): MutableList<SeasonalCameraEventQuota> {
        val matched = filter(items) { candidate: SeasonalCameraEventQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCameraEventQuotas(store: SeasonalCameraEventQuotaStore, query: SeasonalCameraEventQuotaQuery): MutableList<SeasonalCameraEventQuota> {
    return query.apply(store.list())
}
