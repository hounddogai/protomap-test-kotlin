package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled zone notices by code. */
interface ScheduledZoneNoticeStore {
    fun find(code: String): ScheduledZoneNotice?
    fun save(item: ScheduledZoneNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledZoneNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledZoneNoticeStore(
    var items: MutableMap<String, ScheduledZoneNotice>,
    var order: MutableList<String>,
) : ScheduledZoneNoticeStore {
    override fun find(code: String): ScheduledZoneNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledZoneNotice) {
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

    override fun list(): MutableList<ScheduledZoneNotice> {
        val out = mutableListOf<ScheduledZoneNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledZoneNoticeStore(): ScheduledZoneNoticeStore {
    return MemoryScheduledZoneNoticeStore(mutableMapOf<String, ScheduledZoneNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledZoneNoticeStore(
    var inner: ScheduledZoneNoticeStore,
    var cache: TtlCache<ScheduledZoneNotice>,
    var hits: Int,
    var misses: Int,
) : ScheduledZoneNoticeStore {
    override fun find(code: String): ScheduledZoneNotice? {
        val hit = (cache.get(code) as ScheduledZoneNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledZoneNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledZoneNotice> = inner.list()
}

fun newCachedScheduledZoneNoticeStore(inner: ScheduledZoneNoticeStore, ttl: Int, clock: Clock): ScheduledZoneNoticeStore {
    return CachedScheduledZoneNoticeStore(
        inner = inner,
        cache = newTtlCache<ScheduledZoneNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled zone notices. Empty fields match all. */
data class ScheduledZoneNoticeQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledZoneNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledZoneNotice>): MutableList<ScheduledZoneNotice> {
        val matched = filter(items) { candidate: ScheduledZoneNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledZoneNotices(store: ScheduledZoneNoticeStore, query: ScheduledZoneNoticeQuery): MutableList<ScheduledZoneNotice> {
    return query.apply(store.list())
}
