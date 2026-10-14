package com.acme.transport.network.hub.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional hub notices by code. */
interface RegionalHubNoticeStore {
    fun find(code: String): RegionalHubNotice?
    fun save(item: RegionalHubNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalHubNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalHubNoticeStore(
    var items: MutableMap<String, RegionalHubNotice>,
    var order: MutableList<String>,
) : RegionalHubNoticeStore {
    override fun find(code: String): RegionalHubNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalHubNotice) {
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

    override fun list(): MutableList<RegionalHubNotice> {
        val out = mutableListOf<RegionalHubNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalHubNoticeStore(): RegionalHubNoticeStore {
    return MemoryRegionalHubNoticeStore(mutableMapOf<String, RegionalHubNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalHubNoticeStore(
    var inner: RegionalHubNoticeStore,
    var cache: TtlCache<RegionalHubNotice>,
    var hits: Int,
    var misses: Int,
) : RegionalHubNoticeStore {
    override fun find(code: String): RegionalHubNotice? {
        val hit = (cache.get(code) as RegionalHubNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalHubNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalHubNotice> = inner.list()
}

fun newCachedRegionalHubNoticeStore(inner: RegionalHubNoticeStore, ttl: Int, clock: Clock): RegionalHubNoticeStore {
    return CachedRegionalHubNoticeStore(
        inner = inner,
        cache = newTtlCache<RegionalHubNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional hub notices. Empty fields match all. */
data class RegionalHubNoticeQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalHubNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RegionalHubNotice>): MutableList<RegionalHubNotice> {
        val matched = filter(items) { candidate: RegionalHubNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalHubNotices(store: RegionalHubNoticeStore, query: RegionalHubNoticeQuery): MutableList<RegionalHubNotice> {
    return query.apply(store.list())
}
