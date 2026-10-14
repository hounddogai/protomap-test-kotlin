package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive duty period checkpoints by code. */
interface AdaptiveDutyPeriodCheckpointStore {
    fun find(code: String): AdaptiveDutyPeriodCheckpoint?
    fun save(item: AdaptiveDutyPeriodCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDutyPeriodCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDutyPeriodCheckpointStore(
    var items: MutableMap<String, AdaptiveDutyPeriodCheckpoint>,
    var order: MutableList<String>,
) : AdaptiveDutyPeriodCheckpointStore {
    override fun find(code: String): AdaptiveDutyPeriodCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDutyPeriodCheckpoint) {
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

    override fun list(): MutableList<AdaptiveDutyPeriodCheckpoint> {
        val out = mutableListOf<AdaptiveDutyPeriodCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDutyPeriodCheckpointStore(): AdaptiveDutyPeriodCheckpointStore {
    return MemoryAdaptiveDutyPeriodCheckpointStore(
        mutableMapOf<String, AdaptiveDutyPeriodCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDutyPeriodCheckpointStore(
    var inner: AdaptiveDutyPeriodCheckpointStore,
    var cache: TtlCache<AdaptiveDutyPeriodCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDutyPeriodCheckpointStore {
    override fun find(code: String): AdaptiveDutyPeriodCheckpoint? {
        val hit = (cache.get(code) as AdaptiveDutyPeriodCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDutyPeriodCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDutyPeriodCheckpoint> = inner.list()
}

fun newCachedAdaptiveDutyPeriodCheckpointStore(inner: AdaptiveDutyPeriodCheckpointStore, ttl: Int, clock: Clock): AdaptiveDutyPeriodCheckpointStore {
    return CachedAdaptiveDutyPeriodCheckpointStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDutyPeriodCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive duty period checkpoints. Empty fields match all. */
data class AdaptiveDutyPeriodCheckpointQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDutyPeriodCheckpoint): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDutyPeriodCheckpoint>): MutableList<AdaptiveDutyPeriodCheckpoint> {
        val matched = filter(items) { candidate: AdaptiveDutyPeriodCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDutyPeriodCheckpoints(store: AdaptiveDutyPeriodCheckpointStore, query: AdaptiveDutyPeriodCheckpointQuery): MutableList<AdaptiveDutyPeriodCheckpoint> {
    return query.apply(store.list())
}
