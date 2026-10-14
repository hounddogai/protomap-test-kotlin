package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default duty period snapshots by code. */
interface DefaultDutyPeriodSnapshotStore {
    fun find(code: String): DefaultDutyPeriodSnapshot?
    fun save(item: DefaultDutyPeriodSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDutyPeriodSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDutyPeriodSnapshotStore(
    var items: MutableMap<String, DefaultDutyPeriodSnapshot>,
    var order: MutableList<String>,
) : DefaultDutyPeriodSnapshotStore {
    override fun find(code: String): DefaultDutyPeriodSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDutyPeriodSnapshot) {
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

    override fun list(): MutableList<DefaultDutyPeriodSnapshot> {
        val out = mutableListOf<DefaultDutyPeriodSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDutyPeriodSnapshotStore(): DefaultDutyPeriodSnapshotStore {
    return MemoryDefaultDutyPeriodSnapshotStore(
        mutableMapOf<String, DefaultDutyPeriodSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDutyPeriodSnapshotStore(
    var inner: DefaultDutyPeriodSnapshotStore,
    var cache: TtlCache<DefaultDutyPeriodSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultDutyPeriodSnapshotStore {
    override fun find(code: String): DefaultDutyPeriodSnapshot? {
        val hit = (cache.get(code) as DefaultDutyPeriodSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDutyPeriodSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDutyPeriodSnapshot> = inner.list()
}

fun newCachedDefaultDutyPeriodSnapshotStore(inner: DefaultDutyPeriodSnapshotStore, ttl: Int, clock: Clock): DefaultDutyPeriodSnapshotStore {
    return CachedDefaultDutyPeriodSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultDutyPeriodSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default duty period snapshots. Empty fields match all. */
data class DefaultDutyPeriodSnapshotQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDutyPeriodSnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDutyPeriodSnapshot>): MutableList<DefaultDutyPeriodSnapshot> {
        val matched = filter(items) { candidate: DefaultDutyPeriodSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDutyPeriodSnapshots(store: DefaultDutyPeriodSnapshotStore, query: DefaultDutyPeriodSnapshotQuery): MutableList<DefaultDutyPeriodSnapshot> {
    return query.apply(store.list())
}
