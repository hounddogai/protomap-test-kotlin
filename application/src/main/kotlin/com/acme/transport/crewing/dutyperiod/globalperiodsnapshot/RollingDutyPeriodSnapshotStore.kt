package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling duty period snapshots by code. */
interface RollingDutyPeriodSnapshotStore {
    fun find(code: String): RollingDutyPeriodSnapshot?
    fun save(item: RollingDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDutyPeriodSnapshotStore(
    var items: MutableMap<String, RollingDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : RollingDutyPeriodSnapshotStore {
    override fun find(code: String): RollingDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDutyPeriodSnapshot) {
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

    override fun list(): MutableList<RollingDutyPeriodSnapshot> {
        val out = mutableListOf<RollingDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDutyPeriodSnapshotStore(): RollingDutyPeriodSnapshotStore {
    return MemoryRollingDutyPeriodSnapshotStore(
        mutableMapOf<String, RollingDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDutyPeriodSnapshotStore(
    var inner: RollingDutyPeriodSnapshotStore,
    var cache: TtlCache<RollingDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : RollingDutyPeriodSnapshotStore {
    override fun find(code: String): RollingDutyPeriodSnapshot? {
        val hit = (cache.get(code) as RollingDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDutyPeriodSnapshot> = inner.list()
}

fun newCachedRollingDutyPeriodSnapshotStore(inner: RollingDutyPeriodSnapshotStore, ttl: Int, clock: Clock): RollingDutyPeriodSnapshotStore {
    return CachedRollingDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<RollingDutyPeriodSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling duty period snapshots. Empty fields match all. */
data class RollingDutyPeriodSnapshotQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDutyPeriodSnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RollingDutyPeriodSnapshot>): MutableList<RollingDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: RollingDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDutyPeriodSnapshots(store: RollingDutyPeriodSnapshotStore, query: RollingDutyPeriodSnapshotQuery): MutableList<RollingDutyPeriodSnapshot> {
    return query.apply(store.list())
}
