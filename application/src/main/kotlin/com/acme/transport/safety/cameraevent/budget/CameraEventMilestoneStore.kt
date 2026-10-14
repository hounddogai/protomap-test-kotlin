package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event milestones by code. */
interface CameraEventMilestoneStore {
    fun find(code: String): CameraEventMilestone?
    fun save(item: CameraEventMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventMilestoneStore(
    var items: MutableMap<String, CameraEventMilestone>,
    var order: MutableList<String>,
) : CameraEventMilestoneStore {
    override fun find(code: String): CameraEventMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventMilestone) {
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

    override fun list(): MutableList<CameraEventMilestone> {
        val out = mutableListOf<CameraEventMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventMilestoneStore(): CameraEventMilestoneStore {
    return MemoryCameraEventMilestoneStore(
        mutableMapOf<String, CameraEventMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventMilestoneStore(
    var inner: CameraEventMilestoneStore,
    var cache: TtlCache<CameraEventMilestone>,
    var hits: Int,
    var misses: Int,
) : CameraEventMilestoneStore {
    override fun find(code: String): CameraEventMilestone? {
        val hit = (cache.get(code) as CameraEventMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventMilestone> = inner.list()
}

fun newCachedCameraEventMilestoneStore(inner: CameraEventMilestoneStore, ttl: Int, clock: Clock): CameraEventMilestoneStore {
    return CachedCameraEventMilestoneStore(
        inner = inner,
        cache = newTtlCache<CameraEventMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event milestones. Empty fields match all. */
data class CameraEventMilestoneQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventMilestone>): MutableList<CameraEventMilestone> {
        val matched = filter(items) { candidate: CameraEventMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventMilestones(store: CameraEventMilestoneStore, query: CameraEventMilestoneQuery): MutableList<CameraEventMilestone> {
    return query.apply(store.list())
}
