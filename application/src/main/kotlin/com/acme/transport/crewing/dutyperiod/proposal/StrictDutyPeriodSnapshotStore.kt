package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict duty period snapshots by code. */
interface StrictDutyPeriodSnapshotStore {
    fun find(code: String): StrictDutyPeriodSnapshot?
    fun save(item: StrictDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDutyPeriodSnapshotStore(
    var items: MutableMap<String, StrictDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : StrictDutyPeriodSnapshotStore {
    override fun find(code: String): StrictDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDutyPeriodSnapshot) {
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

    override fun list(): MutableList<StrictDutyPeriodSnapshot> {
        val out = mutableListOf<StrictDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDutyPeriodSnapshotStore(): StrictDutyPeriodSnapshotStore {
    return MemoryStrictDutyPeriodSnapshotStore(
        mutableMapOf<String, StrictDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDutyPeriodSnapshotStore(
    var inner: StrictDutyPeriodSnapshotStore,
    var cache: TtlCache<StrictDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : StrictDutyPeriodSnapshotStore {
    override fun find(code: String): StrictDutyPeriodSnapshot? {
        val hit = (cache.get(code) as StrictDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDutyPeriodSnapshot> = inner.list()
}

fun newCachedStrictDutyPeriodSnapshotStore(inner: StrictDutyPeriodSnapshotStore, ttl: Int, clock: Clock): StrictDutyPeriodSnapshotStore {
    return CachedStrictDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<StrictDutyPeriodSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict duty period snapshots. Empty fields match all. */
data class StrictDutyPeriodSnapshotQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDutyPeriodSnapshot): Boolean {
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

    fun apply(items: MutableList<StrictDutyPeriodSnapshot>): MutableList<StrictDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: StrictDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDutyPeriodSnapshots(store: StrictDutyPeriodSnapshotStore, query: StrictDutyPeriodSnapshotQuery): MutableList<StrictDutyPeriodSnapshot> {
    return query.apply(store.list())
}
