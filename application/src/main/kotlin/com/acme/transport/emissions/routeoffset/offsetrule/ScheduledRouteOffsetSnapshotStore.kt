package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset snapshots by code. */
interface ScheduledRouteOffsetSnapshotStore {
    fun find(code: String): ScheduledRouteOffsetSnapshot?
    fun save(item: ScheduledRouteOffsetSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetSnapshotStore(
    var items: MutableMap<String, ScheduledRouteOffsetSnapshot>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetSnapshotStore {
    override fun find(code: String): ScheduledRouteOffsetSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetSnapshot) {
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

    override fun list(): MutableList<ScheduledRouteOffsetSnapshot> {
        val out = mutableListOf<ScheduledRouteOffsetSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetSnapshotStore(): ScheduledRouteOffsetSnapshotStore {
    return MemoryScheduledRouteOffsetSnapshotStore(
        mutableMapOf<String, ScheduledRouteOffsetSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetSnapshotStore(
    var inner: ScheduledRouteOffsetSnapshotStore,
    var cache: TtlCache<ScheduledRouteOffsetSnapshot>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetSnapshotStore {
    override fun find(code: String): ScheduledRouteOffsetSnapshot? {
        val hit = (cache.get(code) as ScheduledRouteOffsetSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetSnapshot> = inner.list()
}

fun newCachedScheduledRouteOffsetSnapshotStore(inner: ScheduledRouteOffsetSnapshotStore, ttl: Int, clock: Clock): ScheduledRouteOffsetSnapshotStore {
    return CachedScheduledRouteOffsetSnapshotStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset snapshots. Empty fields match all. */
data class ScheduledRouteOffsetSnapshotQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledRouteOffsetSnapshot>): MutableList<ScheduledRouteOffsetSnapshot> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetSnapshots(store: ScheduledRouteOffsetSnapshotStore, query: ScheduledRouteOffsetSnapshotQuery): MutableList<ScheduledRouteOffsetSnapshot> {
    return query.apply(store.list())
}
