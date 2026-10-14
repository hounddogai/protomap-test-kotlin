package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tire snapshots by code. */
interface CappedTireSnapshotStore {
    fun find(code: String): CappedTireSnapshot?
    fun save(item: CappedTireSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTireSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTireSnapshotStore(
    var items: MutableMap<String, CappedTireSnapshot>,
    var order: MutableList<String>,
) : CappedTireSnapshotStore {
    override fun find(code: String): CappedTireSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTireSnapshot) {
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

    override fun list(): MutableList<CappedTireSnapshot> {
        val out = mutableListOf<CappedTireSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTireSnapshotStore(): CappedTireSnapshotStore {
    return MemoryCappedTireSnapshotStore(mutableMapOf<String, CappedTireSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTireSnapshotStore(
    var inner: CappedTireSnapshotStore,
    var cache: TtlCache<CappedTireSnapshot>,
    var hits: Int,
    var misses: Int,
) : CappedTireSnapshotStore {
    override fun find(code: String): CappedTireSnapshot? {
        val hit = (cache.get(code) as CappedTireSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTireSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTireSnapshot> = inner.list()
}

fun newCachedCappedTireSnapshotStore(inner: CappedTireSnapshotStore, ttl: Int, clock: Clock): CappedTireSnapshotStore {
    return CachedCappedTireSnapshotStore(
        inner = inner,
        cache = newTtlCache<CappedTireSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tire snapshots. Empty fields match all. */
data class CappedTireSnapshotQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTireSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTireSnapshot>): MutableList<CappedTireSnapshot> {
        val matched = filter(items) { candidate: CappedTireSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTireSnapshots(store: CappedTireSnapshotStore, query: CappedTireSnapshotQuery): MutableList<CappedTireSnapshot> {
    return query.apply(store.list())
}
