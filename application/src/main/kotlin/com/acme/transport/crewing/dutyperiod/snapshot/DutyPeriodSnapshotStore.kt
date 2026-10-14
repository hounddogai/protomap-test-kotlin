package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period snapshots by code. */
interface DutyPeriodSnapshotStore {
    fun find(code: String): DutyPeriodSnapshot?
    fun save(item: DutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodSnapshotStore(
    var items: MutableMap<String, DutyPeriodSnapshot>,
    var order: MutableList<String>,
) : DutyPeriodSnapshotStore {
    override fun find(code: String): DutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodSnapshot) {
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

    override fun list(): MutableList<DutyPeriodSnapshot> {
        val out = mutableListOf<DutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodSnapshotStore(): DutyPeriodSnapshotStore {
    return MemoryDutyPeriodSnapshotStore(mutableMapOf<String, DutyPeriodSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodSnapshotStore(
    var inner: DutyPeriodSnapshotStore,
    var cache: TtlCache<DutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodSnapshotStore {
    override fun find(code: String): DutyPeriodSnapshot? {
        val hit = (cache.get(code) as DutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodSnapshot> = inner.list()
}

fun newCachedDutyPeriodSnapshotStore(inner: DutyPeriodSnapshotStore, ttl: Int, clock: Clock): DutyPeriodSnapshotStore {
    return CachedDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period snapshots. Empty fields match all. */
data class DutyPeriodSnapshotQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DutyPeriodSnapshot>): MutableList<DutyPeriodSnapshot> {
        val matched = filter(items) { candidate: DutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodSnapshots(store: DutyPeriodSnapshotStore, query: DutyPeriodSnapshotQuery): MutableList<DutyPeriodSnapshot> {
    return query.apply(store.list())
}
