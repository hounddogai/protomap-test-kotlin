package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area notices by code. */
interface ServiceAreaNoticeStore {
    fun find(code: String): ServiceAreaNotice?
    fun save(item: ServiceAreaNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaNoticeStore(
    var items: MutableMap<String, ServiceAreaNotice>,
    var order: MutableList<String>,
) : ServiceAreaNoticeStore {
    override fun find(code: String): ServiceAreaNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaNotice) {
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

    override fun list(): MutableList<ServiceAreaNotice> {
        val out = mutableListOf<ServiceAreaNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaNoticeStore(): ServiceAreaNoticeStore {
    return MemoryServiceAreaNoticeStore(mutableMapOf<String, ServiceAreaNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaNoticeStore(
    var inner: ServiceAreaNoticeStore,
    var cache: TtlCache<ServiceAreaNotice>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaNoticeStore {
    override fun find(code: String): ServiceAreaNotice? {
        val hit = (cache.get(code) as ServiceAreaNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaNotice> = inner.list()
}

fun newCachedServiceAreaNoticeStore(inner: ServiceAreaNoticeStore, ttl: Int, clock: Clock): ServiceAreaNoticeStore {
    return CachedServiceAreaNoticeStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area notices. Empty fields match all. */
data class ServiceAreaNoticeQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaNotice>): MutableList<ServiceAreaNotice> {
        val matched = filter(items) { candidate: ServiceAreaNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaNotices(store: ServiceAreaNoticeStore, query: ServiceAreaNoticeQuery): MutableList<ServiceAreaNotice> {
    return query.apply(store.list())
}
