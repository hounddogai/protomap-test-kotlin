package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled qualification thresholds by code. */
interface ScheduledQualificationThresholdStore {
    fun find(code: String): ScheduledQualificationThreshold?
    fun save(item: ScheduledQualificationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledQualificationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledQualificationThresholdStore(
    var items: MutableMap<String, ScheduledQualificationThreshold>,
    var order: MutableList<String>,
) : ScheduledQualificationThresholdStore {
    override fun find(code: String): ScheduledQualificationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledQualificationThreshold) {
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

    override fun list(): MutableList<ScheduledQualificationThreshold> {
        val out = mutableListOf<ScheduledQualificationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledQualificationThresholdStore(): ScheduledQualificationThresholdStore {
    return MemoryScheduledQualificationThresholdStore(
        mutableMapOf<String, ScheduledQualificationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledQualificationThresholdStore(
    var inner: ScheduledQualificationThresholdStore,
    var cache: TtlCache<ScheduledQualificationThreshold>,
    var hits: Int,
    var misses: Int,
) : ScheduledQualificationThresholdStore {
    override fun find(code: String): ScheduledQualificationThreshold? {
        val hit = (cache.get(code) as ScheduledQualificationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledQualificationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledQualificationThreshold> = inner.list()
}

fun newCachedScheduledQualificationThresholdStore(inner: ScheduledQualificationThresholdStore, ttl: Int, clock: Clock): ScheduledQualificationThresholdStore {
    return CachedScheduledQualificationThresholdStore(
        inner = inner,
        cache = newTtlCache<ScheduledQualificationThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled qualification thresholds. Empty fields match all. */
data class ScheduledQualificationThresholdQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledQualificationThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledQualificationThreshold>): MutableList<ScheduledQualificationThreshold> {
        val matched = filter(items) { candidate: ScheduledQualificationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledQualificationThresholds(store: ScheduledQualificationThresholdStore, query: ScheduledQualificationThresholdQuery): MutableList<ScheduledQualificationThreshold> {
    return query.apply(store.list())
}
