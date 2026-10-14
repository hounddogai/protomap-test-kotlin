package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive service plan notices by code. */
interface AdaptiveServicePlanNoticeStore {
    fun find(code: String): AdaptiveServicePlanNotice?
    fun save(item: AdaptiveServicePlanNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServicePlanNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServicePlanNoticeStore(
    var items: MutableMap<String, AdaptiveServicePlanNotice>,
    var order: MutableList<String>,
) : AdaptiveServicePlanNoticeStore {
    override fun find(code: String): AdaptiveServicePlanNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServicePlanNotice) {
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

    override fun list(): MutableList<AdaptiveServicePlanNotice> {
        val out = mutableListOf<AdaptiveServicePlanNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServicePlanNoticeStore(): AdaptiveServicePlanNoticeStore {
    return MemoryAdaptiveServicePlanNoticeStore(
        mutableMapOf<String, AdaptiveServicePlanNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServicePlanNoticeStore(
    var inner: AdaptiveServicePlanNoticeStore,
    var cache: TtlCache<AdaptiveServicePlanNotice>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServicePlanNoticeStore {
    override fun find(code: String): AdaptiveServicePlanNotice? {
        val hit = (cache.get(code) as AdaptiveServicePlanNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServicePlanNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServicePlanNotice> = inner.list()
}

fun newCachedAdaptiveServicePlanNoticeStore(inner: AdaptiveServicePlanNoticeStore, ttl: Int, clock: Clock): AdaptiveServicePlanNoticeStore {
    return CachedAdaptiveServicePlanNoticeStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServicePlanNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service plan notices. Empty fields match all. */
data class AdaptiveServicePlanNoticeQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServicePlanNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveServicePlanNotice>): MutableList<AdaptiveServicePlanNotice> {
        val matched = filter(items) { candidate: AdaptiveServicePlanNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServicePlanNotices(store: AdaptiveServicePlanNoticeStore, query: AdaptiveServicePlanNoticeQuery): MutableList<AdaptiveServicePlanNotice> {
    return query.apply(store.list())
}
