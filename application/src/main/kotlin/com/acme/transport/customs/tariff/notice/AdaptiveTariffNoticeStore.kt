package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tariff notices by code. */
interface AdaptiveTariffNoticeStore {
    fun find(code: String): AdaptiveTariffNotice?
    fun save(item: AdaptiveTariffNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTariffNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTariffNoticeStore(
    var items: MutableMap<String, AdaptiveTariffNotice>,
    var order: MutableList<String>,
) : AdaptiveTariffNoticeStore {
    override fun find(code: String): AdaptiveTariffNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTariffNotice) {
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

    override fun list(): MutableList<AdaptiveTariffNotice> {
        val out = mutableListOf<AdaptiveTariffNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTariffNoticeStore(): AdaptiveTariffNoticeStore {
    return MemoryAdaptiveTariffNoticeStore(
        mutableMapOf<String, AdaptiveTariffNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTariffNoticeStore(
    var inner: AdaptiveTariffNoticeStore,
    var cache: TtlCache<AdaptiveTariffNotice>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTariffNoticeStore {
    override fun find(code: String): AdaptiveTariffNotice? {
        val hit = (cache.get(code) as AdaptiveTariffNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTariffNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTariffNotice> = inner.list()
}

fun newCachedAdaptiveTariffNoticeStore(inner: AdaptiveTariffNoticeStore, ttl: Int, clock: Clock): AdaptiveTariffNoticeStore {
    return CachedAdaptiveTariffNoticeStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTariffNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tariff notices. Empty fields match all. */
data class AdaptiveTariffNoticeQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTariffNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTariffNotice>): MutableList<AdaptiveTariffNotice> {
        val matched = filter(items) { candidate: AdaptiveTariffNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTariffNotices(store: AdaptiveTariffNoticeStore, query: AdaptiveTariffNoticeQuery): MutableList<AdaptiveTariffNotice> {
    return query.apply(store.list())
}
