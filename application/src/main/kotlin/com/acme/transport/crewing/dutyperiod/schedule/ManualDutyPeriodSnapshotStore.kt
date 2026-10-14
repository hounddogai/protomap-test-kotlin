package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual duty period snapshots by code. */
interface ManualDutyPeriodSnapshotStore {
    fun find(code: String): ManualDutyPeriodSnapshot?
    fun save(item: ManualDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDutyPeriodSnapshotStore(
    var items: MutableMap<String, ManualDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : ManualDutyPeriodSnapshotStore {
    override fun find(code: String): ManualDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDutyPeriodSnapshot) {
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

    override fun list(): MutableList<ManualDutyPeriodSnapshot> {
        val out = mutableListOf<ManualDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDutyPeriodSnapshotStore(): ManualDutyPeriodSnapshotStore {
    return MemoryManualDutyPeriodSnapshotStore(
        mutableMapOf<String, ManualDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDutyPeriodSnapshotStore(
    var inner: ManualDutyPeriodSnapshotStore,
    var cache: TtlCache<ManualDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : ManualDutyPeriodSnapshotStore {
    override fun find(code: String): ManualDutyPeriodSnapshot? {
        val hit = (cache.get(code) as ManualDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDutyPeriodSnapshot> = inner.list()
}

fun newCachedManualDutyPeriodSnapshotStore(inner: ManualDutyPeriodSnapshotStore, ttl: Int, clock: Clock): ManualDutyPeriodSnapshotStore {
    return CachedManualDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<ManualDutyPeriodSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual duty period snapshots. Empty fields match all. */
data class ManualDutyPeriodSnapshotQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDutyPeriodSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDutyPeriodSnapshot>): MutableList<ManualDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: ManualDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDutyPeriodSnapshots(store: ManualDutyPeriodSnapshotStore, query: ManualDutyPeriodSnapshotQuery): MutableList<ManualDutyPeriodSnapshot> {
    return query.apply(store.list())
}
