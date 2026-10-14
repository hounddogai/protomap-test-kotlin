package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback duty period snapshots by code. */
interface FallbackDutyPeriodSnapshotStore {
    fun find(code: String): FallbackDutyPeriodSnapshot?
    fun save(item: FallbackDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDutyPeriodSnapshotStore(
    var items: MutableMap<String, FallbackDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : FallbackDutyPeriodSnapshotStore {
    override fun find(code: String): FallbackDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDutyPeriodSnapshot) {
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

    override fun list(): MutableList<FallbackDutyPeriodSnapshot> {
        val out = mutableListOf<FallbackDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDutyPeriodSnapshotStore(): FallbackDutyPeriodSnapshotStore {
    return MemoryFallbackDutyPeriodSnapshotStore(
        mutableMapOf<String, FallbackDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDutyPeriodSnapshotStore(
    var inner: FallbackDutyPeriodSnapshotStore,
    var cache: TtlCache<FallbackDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : FallbackDutyPeriodSnapshotStore {
    override fun find(code: String): FallbackDutyPeriodSnapshot? {
        val hit = (cache.get(code) as FallbackDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDutyPeriodSnapshot> = inner.list()
}

fun newCachedFallbackDutyPeriodSnapshotStore(inner: FallbackDutyPeriodSnapshotStore, ttl: Int, clock: Clock): FallbackDutyPeriodSnapshotStore {
    return CachedFallbackDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<FallbackDutyPeriodSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback duty period snapshots. Empty fields match all. */
data class FallbackDutyPeriodSnapshotQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDutyPeriodSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDutyPeriodSnapshot>): MutableList<FallbackDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: FallbackDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDutyPeriodSnapshots(store: FallbackDutyPeriodSnapshotStore, query: FallbackDutyPeriodSnapshotQuery): MutableList<FallbackDutyPeriodSnapshot> {
    return query.apply(store.list())
}
