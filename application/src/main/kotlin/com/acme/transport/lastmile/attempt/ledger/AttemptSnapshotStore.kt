package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt snapshots by code. */
interface AttemptSnapshotStore {
    fun find(code: String): AttemptSnapshot?
    fun save(item: AttemptSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptSnapshotStore(
    var items: MutableMap<String, AttemptSnapshot>,
    var order: MutableList<String>,
) : AttemptSnapshotStore {
    override fun find(code: String): AttemptSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptSnapshot) {
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

    override fun list(): MutableList<AttemptSnapshot> {
        val out = mutableListOf<AttemptSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptSnapshotStore(): AttemptSnapshotStore {
    return MemoryAttemptSnapshotStore(mutableMapOf<String, AttemptSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptSnapshotStore(
    var inner: AttemptSnapshotStore,
    var cache: TtlCache<AttemptSnapshot>,
    var hits: Int,
    var misses: Int,
) : AttemptSnapshotStore {
    override fun find(code: String): AttemptSnapshot? {
        val hit = (cache.get(code) as AttemptSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptSnapshot> = inner.list()
}

fun newCachedAttemptSnapshotStore(inner: AttemptSnapshotStore, ttl: Int, clock: Clock): AttemptSnapshotStore {
    return CachedAttemptSnapshotStore(
        inner = inner,
        cache = newTtlCache<AttemptSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt snapshots. Empty fields match all. */
data class AttemptSnapshotQuery(
    var region: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptSnapshot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptSnapshot>): MutableList<AttemptSnapshot> {
        val matched = filter(items) { candidate: AttemptSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptSnapshots(store: AttemptSnapshotStore, query: AttemptSnapshotQuery): MutableList<AttemptSnapshot> {
    return query.apply(store.list())
}
