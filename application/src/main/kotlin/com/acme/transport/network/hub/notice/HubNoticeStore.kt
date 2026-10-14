package com.acme.transport.network.hub.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub notices by code. */
interface HubNoticeStore {
    fun find(code: String): HubNotice?
    fun save(item: HubNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubNoticeStore(
    var items: MutableMap<String, HubNotice>,
    var order: MutableList<String>,
) : HubNoticeStore {
    override fun find(code: String): HubNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubNotice) {
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

    override fun list(): MutableList<HubNotice> {
        val out = mutableListOf<HubNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubNoticeStore(): HubNoticeStore {
    return MemoryHubNoticeStore(mutableMapOf<String, HubNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubNoticeStore(
    var inner: HubNoticeStore,
    var cache: TtlCache<HubNotice>,
    var hits: Int,
    var misses: Int,
) : HubNoticeStore {
    override fun find(code: String): HubNotice? {
        val hit = (cache.get(code) as HubNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubNotice> = inner.list()
}

fun newCachedHubNoticeStore(inner: HubNoticeStore, ttl: Int, clock: Clock): HubNoticeStore {
    return CachedHubNoticeStore(
        inner = inner,
        cache = newTtlCache<HubNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub notices. Empty fields match all. */
data class HubNoticeQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubNotice>): MutableList<HubNotice> {
        val matched = filter(items) { candidate: HubNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubNotices(store: HubNoticeStore, query: HubNoticeQuery): MutableList<HubNotice> {
    return query.apply(store.list())
}
