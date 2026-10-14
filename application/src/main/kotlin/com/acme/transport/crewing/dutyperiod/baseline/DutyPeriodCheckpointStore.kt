package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period checkpoints by code. */
interface DutyPeriodCheckpointStore {
    fun find(code: String): DutyPeriodCheckpoint?
    fun save(item: DutyPeriodCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodCheckpointStore(
    var items: MutableMap<String, DutyPeriodCheckpoint>,
    var order: MutableList<String>,
) : DutyPeriodCheckpointStore {
    override fun find(code: String): DutyPeriodCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodCheckpoint) {
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

    override fun list(): MutableList<DutyPeriodCheckpoint> {
        val out = mutableListOf<DutyPeriodCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodCheckpointStore(): DutyPeriodCheckpointStore {
    return MemoryDutyPeriodCheckpointStore(
        mutableMapOf<String, DutyPeriodCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodCheckpointStore(
    var inner: DutyPeriodCheckpointStore,
    var cache: TtlCache<DutyPeriodCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodCheckpointStore {
    override fun find(code: String): DutyPeriodCheckpoint? {
        val hit = (cache.get(code) as DutyPeriodCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodCheckpoint> = inner.list()
}

fun newCachedDutyPeriodCheckpointStore(inner: DutyPeriodCheckpointStore, ttl: Int, clock: Clock): DutyPeriodCheckpointStore {
    return CachedDutyPeriodCheckpointStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period checkpoints. Empty fields match all. */
data class DutyPeriodCheckpointQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodCheckpoint): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DutyPeriodCheckpoint>): MutableList<DutyPeriodCheckpoint> {
        val matched = filter(items) { candidate: DutyPeriodCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodCheckpoints(store: DutyPeriodCheckpointStore, query: DutyPeriodCheckpointQuery): MutableList<DutyPeriodCheckpoint> {
    return query.apply(store.list())
}
