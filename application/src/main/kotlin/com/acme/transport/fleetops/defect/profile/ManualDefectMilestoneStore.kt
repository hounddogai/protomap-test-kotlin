package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual defect milestones by code. */
interface ManualDefectMilestoneStore {
    fun find(code: String): ManualDefectMilestone?
    fun save(item: ManualDefectMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDefectMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDefectMilestoneStore(
    var items: MutableMap<String, ManualDefectMilestone>,
    var order: MutableList<String>,
) : ManualDefectMilestoneStore {
    override fun find(code: String): ManualDefectMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDefectMilestone) {
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

    override fun list(): MutableList<ManualDefectMilestone> {
        val out = mutableListOf<ManualDefectMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDefectMilestoneStore(): ManualDefectMilestoneStore {
    return MemoryManualDefectMilestoneStore(
        mutableMapOf<String, ManualDefectMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDefectMilestoneStore(
    var inner: ManualDefectMilestoneStore,
    var cache: TtlCache<ManualDefectMilestone>,
    var hits: Int,
    var misses: Int,
) : ManualDefectMilestoneStore {
    override fun find(code: String): ManualDefectMilestone? {
        val hit = (cache.get(code) as ManualDefectMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDefectMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDefectMilestone> = inner.list()
}

fun newCachedManualDefectMilestoneStore(inner: ManualDefectMilestoneStore, ttl: Int, clock: Clock): ManualDefectMilestoneStore {
    return CachedManualDefectMilestoneStore(
        inner = inner,
        cache = newTtlCache<ManualDefectMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual defect milestones. Empty fields match all. */
data class ManualDefectMilestoneQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDefectMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDefectMilestone>): MutableList<ManualDefectMilestone> {
        val matched = filter(items) { candidate: ManualDefectMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDefectMilestones(store: ManualDefectMilestoneStore, query: ManualDefectMilestoneQuery): MutableList<ManualDefectMilestone> {
    return query.apply(store.list())
}
