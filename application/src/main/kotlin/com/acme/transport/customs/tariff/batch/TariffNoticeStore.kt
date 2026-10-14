package com.acme.transport.customs.tariff.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff notices by code. */
interface TariffNoticeStore {
    fun find(code: String): TariffNotice?
    fun save(item: TariffNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffNoticeStore(
    var items: MutableMap<String, TariffNotice>,
    var order: MutableList<String>,
) : TariffNoticeStore {
    override fun find(code: String): TariffNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffNotice) {
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

    override fun list(): MutableList<TariffNotice> {
        val out = mutableListOf<TariffNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffNoticeStore(): TariffNoticeStore {
    return MemoryTariffNoticeStore(mutableMapOf<String, TariffNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffNoticeStore(
    var inner: TariffNoticeStore,
    var cache: TtlCache<TariffNotice>,
    var hits: Int,
    var misses: Int,
) : TariffNoticeStore {
    override fun find(code: String): TariffNotice? {
        val hit = (cache.get(code) as TariffNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffNotice> = inner.list()
}

fun newCachedTariffNoticeStore(inner: TariffNoticeStore, ttl: Int, clock: Clock): TariffNoticeStore {
    return CachedTariffNoticeStore(
        inner = inner,
        cache = newTtlCache<TariffNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff notices. Empty fields match all. */
data class TariffNoticeQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffNotice>): MutableList<TariffNotice> {
        val matched = filter(items) { candidate: TariffNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffNotices(store: TariffNoticeStore, query: TariffNoticeQuery): MutableList<TariffNotice> {
    return query.apply(store.list())
}
