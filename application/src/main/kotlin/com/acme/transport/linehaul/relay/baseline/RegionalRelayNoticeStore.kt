package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional relay notices by code. */
interface RegionalRelayNoticeStore {
    fun find(code: String): RegionalRelayNotice?
    fun save(item: RegionalRelayNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRelayNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRelayNoticeStore(
    var items: MutableMap<String, RegionalRelayNotice>,
    var order: MutableList<String>,
) : RegionalRelayNoticeStore {
    override fun find(code: String): RegionalRelayNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRelayNotice) {
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

    override fun list(): MutableList<RegionalRelayNotice> {
        val out = mutableListOf<RegionalRelayNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRelayNoticeStore(): RegionalRelayNoticeStore {
    return MemoryRegionalRelayNoticeStore(mutableMapOf<String, RegionalRelayNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRelayNoticeStore(
    var inner: RegionalRelayNoticeStore,
    var cache: TtlCache<RegionalRelayNotice>,
    var hits: Int,
    var misses: Int,
) : RegionalRelayNoticeStore {
    override fun find(code: String): RegionalRelayNotice? {
        val hit = (cache.get(code) as RegionalRelayNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRelayNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRelayNotice> = inner.list()
}

fun newCachedRegionalRelayNoticeStore(inner: RegionalRelayNoticeStore, ttl: Int, clock: Clock): RegionalRelayNoticeStore {
    return CachedRegionalRelayNoticeStore(
        inner = inner,
        cache = newTtlCache<RegionalRelayNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional relay notices. Empty fields match all. */
data class RegionalRelayNoticeQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRelayNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRelayNotice>): MutableList<RegionalRelayNotice> {
        val matched = filter(items) { candidate: RegionalRelayNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRelayNotices(store: RegionalRelayNoticeStore, query: RegionalRelayNoticeQuery): MutableList<RegionalRelayNotice> {
    return query.apply(store.list())
}
