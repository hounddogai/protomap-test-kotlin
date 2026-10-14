package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual qualification thresholds by code. */
interface ManualQualificationThresholdStore {
    fun find(code: String): ManualQualificationThreshold?
    fun save(item: ManualQualificationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualQualificationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualQualificationThresholdStore(
    var items: MutableMap<String, ManualQualificationThreshold>,
    var order: MutableList<String>,
) : ManualQualificationThresholdStore {
    override fun find(code: String): ManualQualificationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualQualificationThreshold) {
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

    override fun list(): MutableList<ManualQualificationThreshold> {
        val out = mutableListOf<ManualQualificationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualQualificationThresholdStore(): ManualQualificationThresholdStore {
    return MemoryManualQualificationThresholdStore(
        mutableMapOf<String, ManualQualificationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualQualificationThresholdStore(
    var inner: ManualQualificationThresholdStore,
    var cache: TtlCache<ManualQualificationThreshold>,
    var hits: Int,
    var misses: Int,
) : ManualQualificationThresholdStore {
    override fun find(code: String): ManualQualificationThreshold? {
        val hit = (cache.get(code) as ManualQualificationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualQualificationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualQualificationThreshold> = inner.list()
}

fun newCachedManualQualificationThresholdStore(inner: ManualQualificationThresholdStore, ttl: Int, clock: Clock): ManualQualificationThresholdStore {
    return CachedManualQualificationThresholdStore(
        inner = inner,
        cache = newTtlCache<ManualQualificationThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual qualification thresholds. Empty fields match all. */
data class ManualQualificationThresholdQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualQualificationThreshold): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualQualificationThreshold>): MutableList<ManualQualificationThreshold> {
        val matched = filter(items) { candidate: ManualQualificationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualQualificationThresholds(store: ManualQualificationThresholdStore, query: ManualQualificationThresholdQuery): MutableList<ManualQualificationThreshold> {
    return query.apply(store.list())
}
