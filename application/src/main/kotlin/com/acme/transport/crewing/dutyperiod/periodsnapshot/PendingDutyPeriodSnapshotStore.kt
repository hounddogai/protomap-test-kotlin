package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending duty period snapshots by code. */
interface PendingDutyPeriodSnapshotStore {
    fun find(code: String): PendingDutyPeriodSnapshot?
    fun save(item: PendingDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDutyPeriodSnapshotStore(
    var items: MutableMap<String, PendingDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : PendingDutyPeriodSnapshotStore {
    override fun find(code: String): PendingDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDutyPeriodSnapshot) {
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

    override fun list(): MutableList<PendingDutyPeriodSnapshot> {
        val out = mutableListOf<PendingDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDutyPeriodSnapshotStore(): PendingDutyPeriodSnapshotStore {
    return MemoryPendingDutyPeriodSnapshotStore(
        mutableMapOf<String, PendingDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDutyPeriodSnapshotStore(
    var inner: PendingDutyPeriodSnapshotStore,
    var cache: TtlCache<PendingDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingDutyPeriodSnapshotStore {
    override fun find(code: String): PendingDutyPeriodSnapshot? {
        val hit = (cache.get(code) as PendingDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDutyPeriodSnapshot> = inner.list()
}

fun newCachedPendingDutyPeriodSnapshotStore(inner: PendingDutyPeriodSnapshotStore, ttl: Int, clock: Clock): PendingDutyPeriodSnapshotStore {
    return CachedPendingDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingDutyPeriodSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending duty period snapshots. Empty fields match all. */
data class PendingDutyPeriodSnapshotQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDutyPeriodSnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDutyPeriodSnapshot>): MutableList<PendingDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: PendingDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDutyPeriodSnapshots(store: PendingDutyPeriodSnapshotStore, query: PendingDutyPeriodSnapshotQuery): MutableList<PendingDutyPeriodSnapshot> {
    return query.apply(store.list())
}
