package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster snapshots by code. */
interface RosterSnapshotStore {
    fun find(code: String): RosterSnapshot?
    fun save(item: RosterSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterSnapshotStore(
    var items: MutableMap<String, RosterSnapshot>,
    var order: MutableList<String>,
) : RosterSnapshotStore {
    override fun find(code: String): RosterSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterSnapshot) {
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

    override fun list(): MutableList<RosterSnapshot> {
        val out = mutableListOf<RosterSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterSnapshotStore(): RosterSnapshotStore {
    return MemoryRosterSnapshotStore(mutableMapOf<String, RosterSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterSnapshotStore(
    var inner: RosterSnapshotStore,
    var cache: TtlCache<RosterSnapshot>,
    var hits: Int,
    var misses: Int,
) : RosterSnapshotStore {
    override fun find(code: String): RosterSnapshot? {
        val hit = (cache.get(code) as RosterSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterSnapshot> = inner.list()
}

fun newCachedRosterSnapshotStore(inner: RosterSnapshotStore, ttl: Int, clock: Clock): RosterSnapshotStore {
    return CachedRosterSnapshotStore(
        inner = inner,
        cache = newTtlCache<RosterSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster snapshots. Empty fields match all. */
data class RosterSnapshotQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterSnapshot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterSnapshot>): MutableList<RosterSnapshot> {
        val matched = filter(items) { candidate: RosterSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterSnapshots(store: RosterSnapshotStore, query: RosterSnapshotQuery): MutableList<RosterSnapshot> {
    return query.apply(store.list())
}
