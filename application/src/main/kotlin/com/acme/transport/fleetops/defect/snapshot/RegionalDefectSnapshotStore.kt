package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect snapshots by code. */
interface RegionalDefectSnapshotStore {
    fun find(code: String): RegionalDefectSnapshot?
    fun save(item: RegionalDefectSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectSnapshotStore(
    var items: MutableMap<String, RegionalDefectSnapshot>,
    var order: MutableList<String>,
) : RegionalDefectSnapshotStore {
    override fun find(code: String): RegionalDefectSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectSnapshot) {
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

    override fun list(): MutableList<RegionalDefectSnapshot> {
        val out = mutableListOf<RegionalDefectSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectSnapshotStore(): RegionalDefectSnapshotStore {
    return MemoryRegionalDefectSnapshotStore(
        mutableMapOf<String, RegionalDefectSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectSnapshotStore(
    var inner: RegionalDefectSnapshotStore,
    var cache: TtlCache<RegionalDefectSnapshot>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectSnapshotStore {
    override fun find(code: String): RegionalDefectSnapshot? {
        val hit = (cache.get(code) as RegionalDefectSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectSnapshot> = inner.list()
}

fun newCachedRegionalDefectSnapshotStore(inner: RegionalDefectSnapshotStore, ttl: Int, clock: Clock): RegionalDefectSnapshotStore {
    return CachedRegionalDefectSnapshotStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect snapshots. Empty fields match all. */
data class RegionalDefectSnapshotQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectSnapshot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDefectSnapshot>): MutableList<RegionalDefectSnapshot> {
        val matched = filter(items) { candidate: RegionalDefectSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectSnapshots(store: RegionalDefectSnapshotStore, query: RegionalDefectSnapshotQuery): MutableList<RegionalDefectSnapshot> {
    return query.apply(store.list())
}
