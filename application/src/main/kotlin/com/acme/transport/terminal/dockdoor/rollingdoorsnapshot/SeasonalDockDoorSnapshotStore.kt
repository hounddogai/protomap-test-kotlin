package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dock door snapshots by code. */
interface SeasonalDockDoorSnapshotStore {
    fun find(code: String): SeasonalDockDoorSnapshot?
    fun save(item: SeasonalDockDoorSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDockDoorSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDockDoorSnapshotStore(
    var items: MutableMap<String, SeasonalDockDoorSnapshot>,
    var order: MutableList<String>,
) : SeasonalDockDoorSnapshotStore {
    override fun find(code: String): SeasonalDockDoorSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDockDoorSnapshot) {
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

    override fun list(): MutableList<SeasonalDockDoorSnapshot> {
        val out = mutableListOf<SeasonalDockDoorSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDockDoorSnapshotStore(): SeasonalDockDoorSnapshotStore {
    return MemorySeasonalDockDoorSnapshotStore(
        mutableMapOf<String, SeasonalDockDoorSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDockDoorSnapshotStore(
    var inner: SeasonalDockDoorSnapshotStore,
    var cache: TtlCache<SeasonalDockDoorSnapshot>,
    var hits: Int,
    var misses: Int,
) : SeasonalDockDoorSnapshotStore {
    override fun find(code: String): SeasonalDockDoorSnapshot? {
        val hit = (cache.get(code) as SeasonalDockDoorSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDockDoorSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDockDoorSnapshot> = inner.list()
}

fun newCachedSeasonalDockDoorSnapshotStore(inner: SeasonalDockDoorSnapshotStore, ttl: Int, clock: Clock): SeasonalDockDoorSnapshotStore {
    return CachedSeasonalDockDoorSnapshotStore(
        inner = inner,
        cache = newTtlCache<SeasonalDockDoorSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dock door snapshots. Empty fields match all. */
data class SeasonalDockDoorSnapshotQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDockDoorSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDockDoorSnapshot>): MutableList<SeasonalDockDoorSnapshot> {
        val matched = filter(items) { candidate: SeasonalDockDoorSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDockDoorSnapshots(store: SeasonalDockDoorSnapshotStore, query: SeasonalDockDoorSnapshotQuery): MutableList<SeasonalDockDoorSnapshot> {
    return query.apply(store.list())
}
