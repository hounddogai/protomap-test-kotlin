package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient zone notices by code. */
interface LenientZoneNoticeStore {
    fun find(code: String): LenientZoneNotice?
    fun save(item: LenientZoneNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientZoneNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientZoneNoticeStore(
    var items: MutableMap<String, LenientZoneNotice>,
    var order: MutableList<String>,
) : LenientZoneNoticeStore {
    override fun find(code: String): LenientZoneNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientZoneNotice) {
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

    override fun list(): MutableList<LenientZoneNotice> {
        val out = mutableListOf<LenientZoneNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientZoneNoticeStore(): LenientZoneNoticeStore {
    return MemoryLenientZoneNoticeStore(mutableMapOf<String, LenientZoneNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientZoneNoticeStore(
    var inner: LenientZoneNoticeStore,
    var cache: TtlCache<LenientZoneNotice>,
    var hits: Int,
    var misses: Int,
) : LenientZoneNoticeStore {
    override fun find(code: String): LenientZoneNotice? {
        val hit = (cache.get(code) as LenientZoneNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientZoneNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientZoneNotice> = inner.list()
}

fun newCachedLenientZoneNoticeStore(inner: LenientZoneNoticeStore, ttl: Int, clock: Clock): LenientZoneNoticeStore {
    return CachedLenientZoneNoticeStore(
        inner = inner,
        cache = newTtlCache<LenientZoneNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient zone notices. Empty fields match all. */
data class LenientZoneNoticeQuery(
    var country: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientZoneNotice): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientZoneNotice>): MutableList<LenientZoneNotice> {
        val matched = filter(items) { candidate: LenientZoneNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientZoneNotices(store: LenientZoneNoticeStore, query: LenientZoneNoticeQuery): MutableList<LenientZoneNotice> {
    return query.apply(store.list())
}
