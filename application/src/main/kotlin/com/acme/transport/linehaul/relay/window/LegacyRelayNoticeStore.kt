package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy relay notices by code. */
interface LegacyRelayNoticeStore {
    fun find(code: String): LegacyRelayNotice?
    fun save(item: LegacyRelayNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRelayNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRelayNoticeStore(
    var items: MutableMap<String, LegacyRelayNotice>,
    var order: MutableList<String>,
) : LegacyRelayNoticeStore {
    override fun find(code: String): LegacyRelayNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRelayNotice) {
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

    override fun list(): MutableList<LegacyRelayNotice> {
        val out = mutableListOf<LegacyRelayNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRelayNoticeStore(): LegacyRelayNoticeStore {
    return MemoryLegacyRelayNoticeStore(mutableMapOf<String, LegacyRelayNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRelayNoticeStore(
    var inner: LegacyRelayNoticeStore,
    var cache: TtlCache<LegacyRelayNotice>,
    var hits: Int,
    var misses: Int,
) : LegacyRelayNoticeStore {
    override fun find(code: String): LegacyRelayNotice? {
        val hit = (cache.get(code) as LegacyRelayNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRelayNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRelayNotice> = inner.list()
}

fun newCachedLegacyRelayNoticeStore(inner: LegacyRelayNoticeStore, ttl: Int, clock: Clock): LegacyRelayNoticeStore {
    return CachedLegacyRelayNoticeStore(
        inner = inner,
        cache = newTtlCache<LegacyRelayNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy relay notices. Empty fields match all. */
data class LegacyRelayNoticeQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRelayNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRelayNotice>): MutableList<LegacyRelayNotice> {
        val matched = filter(items) { candidate: LegacyRelayNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRelayNotices(store: LegacyRelayNoticeStore, query: LegacyRelayNoticeQuery): MutableList<LegacyRelayNotice> {
    return query.apply(store.list())
}
