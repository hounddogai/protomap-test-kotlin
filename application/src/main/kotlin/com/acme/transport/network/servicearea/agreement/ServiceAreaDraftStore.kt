package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area drafts by code. */
interface ServiceAreaDraftStore {
    fun find(code: String): ServiceAreaDraft?
    fun save(item: ServiceAreaDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaDraftStore(
    var items: MutableMap<String, ServiceAreaDraft>,
    var order: MutableList<String>,
) : ServiceAreaDraftStore {
    override fun find(code: String): ServiceAreaDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaDraft) {
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

    override fun list(): MutableList<ServiceAreaDraft> {
        val out = mutableListOf<ServiceAreaDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaDraftStore(): ServiceAreaDraftStore {
    return MemoryServiceAreaDraftStore(mutableMapOf<String, ServiceAreaDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaDraftStore(
    var inner: ServiceAreaDraftStore,
    var cache: TtlCache<ServiceAreaDraft>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaDraftStore {
    override fun find(code: String): ServiceAreaDraft? {
        val hit = (cache.get(code) as ServiceAreaDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaDraft> = inner.list()
}

fun newCachedServiceAreaDraftStore(inner: ServiceAreaDraftStore, ttl: Int, clock: Clock): ServiceAreaDraftStore {
    return CachedServiceAreaDraftStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area drafts. Empty fields match all. */
data class ServiceAreaDraftQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ServiceAreaDraft>): MutableList<ServiceAreaDraft> {
        val matched = filter(items) { candidate: ServiceAreaDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaDrafts(store: ServiceAreaDraftStore, query: ServiceAreaDraftQuery): MutableList<ServiceAreaDraft> {
    return query.apply(store.list())
}
