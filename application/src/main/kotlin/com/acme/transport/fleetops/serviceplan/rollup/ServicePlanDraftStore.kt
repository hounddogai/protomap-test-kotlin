package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan drafts by code. */
interface ServicePlanDraftStore {
    fun find(code: String): ServicePlanDraft?
    fun save(item: ServicePlanDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanDraftStore(
    var items: MutableMap<String, ServicePlanDraft>,
    var order: MutableList<String>,
) : ServicePlanDraftStore {
    override fun find(code: String): ServicePlanDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanDraft) {
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

    override fun list(): MutableList<ServicePlanDraft> {
        val out = mutableListOf<ServicePlanDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanDraftStore(): ServicePlanDraftStore {
    return MemoryServicePlanDraftStore(mutableMapOf<String, ServicePlanDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanDraftStore(
    var inner: ServicePlanDraftStore,
    var cache: TtlCache<ServicePlanDraft>,
    var hits: Int,
    var misses: Int,
) : ServicePlanDraftStore {
    override fun find(code: String): ServicePlanDraft? {
        val hit = (cache.get(code) as ServicePlanDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanDraft> = inner.list()
}

fun newCachedServicePlanDraftStore(inner: ServicePlanDraftStore, ttl: Int, clock: Clock): ServicePlanDraftStore {
    return CachedServicePlanDraftStore(
        inner = inner,
        cache = newTtlCache<ServicePlanDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan drafts. Empty fields match all. */
data class ServicePlanDraftQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanDraft>): MutableList<ServicePlanDraft> {
        val matched = filter(items) { candidate: ServicePlanDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanDrafts(store: ServicePlanDraftStore, query: ServicePlanDraftQuery): MutableList<ServicePlanDraft> {
    return query.apply(store.list())
}
