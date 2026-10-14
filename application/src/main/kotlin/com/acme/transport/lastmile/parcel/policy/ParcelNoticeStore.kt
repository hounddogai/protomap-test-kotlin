package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel notices by code. */
interface ParcelNoticeStore {
    fun find(code: String): ParcelNotice?
    fun save(item: ParcelNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelNoticeStore(
    var items: MutableMap<String, ParcelNotice>,
    var order: MutableList<String>,
) : ParcelNoticeStore {
    override fun find(code: String): ParcelNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelNotice) {
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

    override fun list(): MutableList<ParcelNotice> {
        val out = mutableListOf<ParcelNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelNoticeStore(): ParcelNoticeStore {
    return MemoryParcelNoticeStore(mutableMapOf<String, ParcelNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelNoticeStore(
    var inner: ParcelNoticeStore,
    var cache: TtlCache<ParcelNotice>,
    var hits: Int,
    var misses: Int,
) : ParcelNoticeStore {
    override fun find(code: String): ParcelNotice? {
        val hit = (cache.get(code) as ParcelNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelNotice> = inner.list()
}

fun newCachedParcelNoticeStore(inner: ParcelNoticeStore, ttl: Int, clock: Clock): ParcelNoticeStore {
    return CachedParcelNoticeStore(
        inner = inner,
        cache = newTtlCache<ParcelNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel notices. Empty fields match all. */
data class ParcelNoticeQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelNotice>): MutableList<ParcelNotice> {
        val matched = filter(items) { candidate: ParcelNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelNotices(store: ParcelNoticeStore, query: ParcelNoticeQuery): MutableList<ParcelNotice> {
    return query.apply(store.list())
}
