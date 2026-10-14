package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary proof of delivery schedules by code. */
interface PrimaryProofOfDeliveryScheduleStore {
    fun find(code: String): PrimaryProofOfDeliverySchedule?
    fun save(item: PrimaryProofOfDeliverySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryProofOfDeliverySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryProofOfDeliveryScheduleStore(
    var items: MutableMap<String, PrimaryProofOfDeliverySchedule>,
    var order: MutableList<String>,
) : PrimaryProofOfDeliveryScheduleStore {
    override fun find(code: String): PrimaryProofOfDeliverySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryProofOfDeliverySchedule) {
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

    override fun list(): MutableList<PrimaryProofOfDeliverySchedule> {
        val out = mutableListOf<PrimaryProofOfDeliverySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryProofOfDeliveryScheduleStore(): PrimaryProofOfDeliveryScheduleStore {
    return MemoryPrimaryProofOfDeliveryScheduleStore(
        mutableMapOf<String, PrimaryProofOfDeliverySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryProofOfDeliveryScheduleStore(
    var inner: PrimaryProofOfDeliveryScheduleStore,
    var cache: TtlCache<PrimaryProofOfDeliverySchedule>,
    var hits: Int,
    var misses: Int,
) : PrimaryProofOfDeliveryScheduleStore {
    override fun find(code: String): PrimaryProofOfDeliverySchedule? {
        val hit = (cache.get(code) as PrimaryProofOfDeliverySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryProofOfDeliverySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryProofOfDeliverySchedule> = inner.list()
}

fun newCachedPrimaryProofOfDeliveryScheduleStore(inner: PrimaryProofOfDeliveryScheduleStore, ttl: Int, clock: Clock): PrimaryProofOfDeliveryScheduleStore {
    return CachedPrimaryProofOfDeliveryScheduleStore(
        inner = inner,
        cache = newTtlCache<PrimaryProofOfDeliverySchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary proof of delivery schedules. Empty fields match all. */
data class PrimaryProofOfDeliveryScheduleQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryProofOfDeliverySchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryProofOfDeliverySchedule>): MutableList<PrimaryProofOfDeliverySchedule> {
        val matched = filter(items) { candidate: PrimaryProofOfDeliverySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryProofOfDeliverySchedules(store: PrimaryProofOfDeliveryScheduleStore, query: PrimaryProofOfDeliveryScheduleQuery): MutableList<PrimaryProofOfDeliverySchedule> {
    return query.apply(store.list())
}
