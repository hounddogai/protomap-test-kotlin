package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident notices by code. */
interface IncidentNoticeStore {
    fun find(code: String): IncidentNotice?
    fun save(item: IncidentNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentNoticeStore(
    var items: MutableMap<String, IncidentNotice>,
    var order: MutableList<String>,
) : IncidentNoticeStore {
    override fun find(code: String): IncidentNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentNotice) {
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

    override fun list(): MutableList<IncidentNotice> {
        val out = mutableListOf<IncidentNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentNoticeStore(): IncidentNoticeStore {
    return MemoryIncidentNoticeStore(mutableMapOf<String, IncidentNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentNoticeStore(
    var inner: IncidentNoticeStore,
    var cache: TtlCache<IncidentNotice>,
    var hits: Int,
    var misses: Int,
) : IncidentNoticeStore {
    override fun find(code: String): IncidentNotice? {
        val hit = (cache.get(code) as IncidentNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentNotice> = inner.list()
}

fun newCachedIncidentNoticeStore(inner: IncidentNoticeStore, ttl: Int, clock: Clock): IncidentNoticeStore {
    return CachedIncidentNoticeStore(
        inner = inner,
        cache = newTtlCache<IncidentNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident notices. Empty fields match all. */
data class IncidentNoticeQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentNotice>): MutableList<IncidentNotice> {
        val matched = filter(items) { candidate: IncidentNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentNotices(store: IncidentNoticeStore, query: IncidentNoticeQuery): MutableList<IncidentNotice> {
    return query.apply(store.list())
}
