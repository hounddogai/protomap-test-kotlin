package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary service plan notices by code. */
interface PrimaryServicePlanNoticeStore {
    fun find(code: String): PrimaryServicePlanNotice?
    fun save(item: PrimaryServicePlanNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServicePlanNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServicePlanNoticeStore(
    var items: MutableMap<String, PrimaryServicePlanNotice>,
    var order: MutableList<String>,
) : PrimaryServicePlanNoticeStore {
    override fun find(code: String): PrimaryServicePlanNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServicePlanNotice) {
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

    override fun list(): MutableList<PrimaryServicePlanNotice> {
        val out = mutableListOf<PrimaryServicePlanNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServicePlanNoticeStore(): PrimaryServicePlanNoticeStore {
    return MemoryPrimaryServicePlanNoticeStore(
        mutableMapOf<String, PrimaryServicePlanNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServicePlanNoticeStore(
    var inner: PrimaryServicePlanNoticeStore,
    var cache: TtlCache<PrimaryServicePlanNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryServicePlanNoticeStore {
    override fun find(code: String): PrimaryServicePlanNotice? {
        val hit = (cache.get(code) as PrimaryServicePlanNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServicePlanNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServicePlanNotice> = inner.list()
}

fun newCachedPrimaryServicePlanNoticeStore(inner: PrimaryServicePlanNoticeStore, ttl: Int, clock: Clock): PrimaryServicePlanNoticeStore {
    return CachedPrimaryServicePlanNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryServicePlanNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service plan notices. Empty fields match all. */
data class PrimaryServicePlanNoticeQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServicePlanNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryServicePlanNotice>): MutableList<PrimaryServicePlanNotice> {
        val matched = filter(items) { candidate: PrimaryServicePlanNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServicePlanNotices(store: PrimaryServicePlanNoticeStore, query: PrimaryServicePlanNoticeQuery): MutableList<PrimaryServicePlanNotice> {
    return query.apply(store.list())
}
