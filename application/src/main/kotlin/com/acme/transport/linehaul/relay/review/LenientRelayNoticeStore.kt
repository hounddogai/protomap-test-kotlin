package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient relay notices by code. */
interface LenientRelayNoticeStore {
    fun find(code: String): LenientRelayNotice?
    fun save(item: LenientRelayNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRelayNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRelayNoticeStore(
    var items: MutableMap<String, LenientRelayNotice>,
    var order: MutableList<String>,
) : LenientRelayNoticeStore {
    override fun find(code: String): LenientRelayNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRelayNotice) {
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

    override fun list(): MutableList<LenientRelayNotice> {
        val out = mutableListOf<LenientRelayNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRelayNoticeStore(): LenientRelayNoticeStore {
    return MemoryLenientRelayNoticeStore(mutableMapOf<String, LenientRelayNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRelayNoticeStore(
    var inner: LenientRelayNoticeStore,
    var cache: TtlCache<LenientRelayNotice>,
    var hits: Int,
    var misses: Int,
) : LenientRelayNoticeStore {
    override fun find(code: String): LenientRelayNotice? {
        val hit = (cache.get(code) as LenientRelayNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRelayNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRelayNotice> = inner.list()
}

fun newCachedLenientRelayNoticeStore(inner: LenientRelayNoticeStore, ttl: Int, clock: Clock): LenientRelayNoticeStore {
    return CachedLenientRelayNoticeStore(
        inner = inner,
        cache = newTtlCache<LenientRelayNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient relay notices. Empty fields match all. */
data class LenientRelayNoticeQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRelayNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRelayNotice>): MutableList<LenientRelayNotice> {
        val matched = filter(items) { candidate: LenientRelayNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRelayNotices(store: LenientRelayNoticeStore, query: LenientRelayNoticeQuery): MutableList<LenientRelayNotice> {
    return query.apply(store.list())
}
