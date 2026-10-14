package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification milestones by code. */
interface QualificationMilestoneStore {
    fun find(code: String): QualificationMilestone?
    fun save(item: QualificationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationMilestoneStore(
    var items: MutableMap<String, QualificationMilestone>,
    var order: MutableList<String>,
) : QualificationMilestoneStore {
    override fun find(code: String): QualificationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationMilestone) {
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

    override fun list(): MutableList<QualificationMilestone> {
        val out = mutableListOf<QualificationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationMilestoneStore(): QualificationMilestoneStore {
    return MemoryQualificationMilestoneStore(
        mutableMapOf<String, QualificationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationMilestoneStore(
    var inner: QualificationMilestoneStore,
    var cache: TtlCache<QualificationMilestone>,
    var hits: Int,
    var misses: Int,
) : QualificationMilestoneStore {
    override fun find(code: String): QualificationMilestone? {
        val hit = (cache.get(code) as QualificationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationMilestone> = inner.list()
}

fun newCachedQualificationMilestoneStore(inner: QualificationMilestoneStore, ttl: Int, clock: Clock): QualificationMilestoneStore {
    return CachedQualificationMilestoneStore(
        inner = inner,
        cache = newTtlCache<QualificationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification milestones. Empty fields match all. */
data class QualificationMilestoneQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationMilestone): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationMilestone>): MutableList<QualificationMilestone> {
        val matched = filter(items) { candidate: QualificationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationMilestones(store: QualificationMilestoneStore, query: QualificationMilestoneQuery): MutableList<QualificationMilestone> {
    return query.apply(store.list())
}
