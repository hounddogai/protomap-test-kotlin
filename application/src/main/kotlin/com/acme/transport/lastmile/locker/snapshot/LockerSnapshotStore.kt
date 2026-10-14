package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker snapshots by code. */
interface LockerSnapshotStore {
    fun find(code: String): LockerSnapshot?
    fun save(item: LockerSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerSnapshotStore(
    var items: MutableMap<String, LockerSnapshot>,
    var order: MutableList<String>,
) : LockerSnapshotStore {
    override fun find(code: String): LockerSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerSnapshot) {
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

    override fun list(): MutableList<LockerSnapshot> {
        val out = mutableListOf<LockerSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerSnapshotStore(): LockerSnapshotStore {
    return MemoryLockerSnapshotStore(mutableMapOf<String, LockerSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerSnapshotStore(
    var inner: LockerSnapshotStore,
    var cache: TtlCache<LockerSnapshot>,
    var hits: Int,
    var misses: Int,
) : LockerSnapshotStore {
    override fun find(code: String): LockerSnapshot? {
        val hit = (cache.get(code) as LockerSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerSnapshot> = inner.list()
}

fun newCachedLockerSnapshotStore(inner: LockerSnapshotStore, ttl: Int, clock: Clock): LockerSnapshotStore {
    return CachedLockerSnapshotStore(
        inner = inner,
        cache = newTtlCache<LockerSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker snapshots. Empty fields match all. */
data class LockerSnapshotQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerSnapshot>): MutableList<LockerSnapshot> {
        val matched = filter(items) { candidate: LockerSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerSnapshots(store: LockerSnapshotStore, query: LockerSnapshotQuery): MutableList<LockerSnapshot> {
    return query.apply(store.list())
}
