package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event plans by code. */
interface CameraEventPlanStore {
    fun find(code: String): CameraEventPlan?
    fun save(item: CameraEventPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventPlanStore(
    var items: MutableMap<String, CameraEventPlan>,
    var order: MutableList<String>,
) : CameraEventPlanStore {
    override fun find(code: String): CameraEventPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventPlan) {
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

    override fun list(): MutableList<CameraEventPlan> {
        val out = mutableListOf<CameraEventPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventPlanStore(): CameraEventPlanStore {
    return MemoryCameraEventPlanStore(mutableMapOf<String, CameraEventPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventPlanStore(
    var inner: CameraEventPlanStore,
    var cache: TtlCache<CameraEventPlan>,
    var hits: Int,
    var misses: Int,
) : CameraEventPlanStore {
    override fun find(code: String): CameraEventPlan? {
        val hit = (cache.get(code) as CameraEventPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventPlan> = inner.list()
}

fun newCachedCameraEventPlanStore(inner: CameraEventPlanStore, ttl: Int, clock: Clock): CameraEventPlanStore {
    return CachedCameraEventPlanStore(
        inner = inner,
        cache = newTtlCache<CameraEventPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event plans. Empty fields match all. */
data class CameraEventPlanQuery(
    var country: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventPlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<CameraEventPlan>): MutableList<CameraEventPlan> {
        val matched = filter(items) { candidate: CameraEventPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventPlans(store: CameraEventPlanStore, query: CameraEventPlanQuery): MutableList<CameraEventPlan> {
    return query.apply(store.list())
}
