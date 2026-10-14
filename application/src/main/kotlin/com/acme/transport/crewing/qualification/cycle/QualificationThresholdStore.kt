package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification thresholds by code. */
interface QualificationThresholdStore {
    fun find(code: String): QualificationThreshold?
    fun save(item: QualificationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationThresholdStore(
    var items: MutableMap<String, QualificationThreshold>,
    var order: MutableList<String>,
) : QualificationThresholdStore {
    override fun find(code: String): QualificationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationThreshold) {
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

    override fun list(): MutableList<QualificationThreshold> {
        val out = mutableListOf<QualificationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationThresholdStore(): QualificationThresholdStore {
    return MemoryQualificationThresholdStore(
        mutableMapOf<String, QualificationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationThresholdStore(
    var inner: QualificationThresholdStore,
    var cache: TtlCache<QualificationThreshold>,
    var hits: Int,
    var misses: Int,
) : QualificationThresholdStore {
    override fun find(code: String): QualificationThreshold? {
        val hit = (cache.get(code) as QualificationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationThreshold> = inner.list()
}

fun newCachedQualificationThresholdStore(inner: QualificationThresholdStore, ttl: Int, clock: Clock): QualificationThresholdStore {
    return CachedQualificationThresholdStore(
        inner = inner,
        cache = newTtlCache<QualificationThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification thresholds. Empty fields match all. */
data class QualificationThresholdQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationThreshold): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationThreshold>): MutableList<QualificationThreshold> {
        val matched = filter(items) { candidate: QualificationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationThresholds(store: QualificationThresholdStore, query: QualificationThresholdQuery): MutableList<QualificationThreshold> {
    return query.apply(store.list())
}
