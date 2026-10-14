package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay notices by code. */
interface RelayNoticeStore {
    fun find(code: String): RelayNotice?
    fun save(item: RelayNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayNoticeStore(
    var items: MutableMap<String, RelayNotice>,
    var order: MutableList<String>,
) : RelayNoticeStore {
    override fun find(code: String): RelayNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayNotice) {
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

    override fun list(): MutableList<RelayNotice> {
        val out = mutableListOf<RelayNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayNoticeStore(): RelayNoticeStore {
    return MemoryRelayNoticeStore(mutableMapOf<String, RelayNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayNoticeStore(
    var inner: RelayNoticeStore,
    var cache: TtlCache<RelayNotice>,
    var hits: Int,
    var misses: Int,
) : RelayNoticeStore {
    override fun find(code: String): RelayNotice? {
        val hit = (cache.get(code) as RelayNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayNotice> = inner.list()
}

fun newCachedRelayNoticeStore(inner: RelayNoticeStore, ttl: Int, clock: Clock): RelayNoticeStore {
    return CachedRelayNoticeStore(
        inner = inner,
        cache = newTtlCache<RelayNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay notices. Empty fields match all. */
data class RelayNoticeQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayNotice>): MutableList<RelayNotice> {
        val matched = filter(items) { candidate: RelayNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayNotices(store: RelayNoticeStore, query: RelayNoticeQuery): MutableList<RelayNotice> {
    return query.apply(store.list())
}
