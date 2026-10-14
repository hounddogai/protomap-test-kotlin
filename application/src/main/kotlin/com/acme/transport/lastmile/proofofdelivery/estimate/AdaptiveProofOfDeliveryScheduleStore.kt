package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive proof of delivery schedules by code. */
interface AdaptiveProofOfDeliveryScheduleStore {
    fun find(code: String): AdaptiveProofOfDeliverySchedule?
    fun save(item: AdaptiveProofOfDeliverySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveProofOfDeliverySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveProofOfDeliveryScheduleStore(
    var items: MutableMap<String, AdaptiveProofOfDeliverySchedule>,
    var order: MutableList<String>,
) : AdaptiveProofOfDeliveryScheduleStore {
    override fun find(code: String): AdaptiveProofOfDeliverySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveProofOfDeliverySchedule) {
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

    override fun list(): MutableList<AdaptiveProofOfDeliverySchedule> {
        val out = mutableListOf<AdaptiveProofOfDeliverySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveProofOfDeliveryScheduleStore(): AdaptiveProofOfDeliveryScheduleStore {
    return MemoryAdaptiveProofOfDeliveryScheduleStore(
        mutableMapOf<String, AdaptiveProofOfDeliverySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveProofOfDeliveryScheduleStore(
    var inner: AdaptiveProofOfDeliveryScheduleStore,
    var cache: TtlCache<AdaptiveProofOfDeliverySchedule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveProofOfDeliveryScheduleStore {
    override fun find(code: String): AdaptiveProofOfDeliverySchedule? {
        val hit = (cache.get(code) as AdaptiveProofOfDeliverySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveProofOfDeliverySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveProofOfDeliverySchedule> = inner.list()
}

fun newCachedAdaptiveProofOfDeliveryScheduleStore(inner: AdaptiveProofOfDeliveryScheduleStore, ttl: Int, clock: Clock): AdaptiveProofOfDeliveryScheduleStore {
    return CachedAdaptiveProofOfDeliveryScheduleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveProofOfDeliverySchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive proof of delivery schedules. Empty fields match all. */
data class AdaptiveProofOfDeliveryScheduleQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveProofOfDeliverySchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveProofOfDeliverySchedule>): MutableList<AdaptiveProofOfDeliverySchedule> {
        val matched = filter(items) { candidate: AdaptiveProofOfDeliverySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveProofOfDeliverySchedules(store: AdaptiveProofOfDeliveryScheduleStore, query: AdaptiveProofOfDeliveryScheduleQuery): MutableList<AdaptiveProofOfDeliverySchedule> {
    return query.apply(store.list())
}
