package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer snapshots by code. */
interface TrailerSnapshotStore {
    fun find(code: String): TrailerSnapshot?
    fun save(item: TrailerSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerSnapshotStore(
    var items: MutableMap<String, TrailerSnapshot>,
    var order: MutableList<String>,
) : TrailerSnapshotStore {
    override fun find(code: String): TrailerSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerSnapshot) {
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

    override fun list(): MutableList<TrailerSnapshot> {
        val out = mutableListOf<TrailerSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerSnapshotStore(): TrailerSnapshotStore {
    return MemoryTrailerSnapshotStore(mutableMapOf<String, TrailerSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerSnapshotStore(
    var inner: TrailerSnapshotStore,
    var cache: TtlCache<TrailerSnapshot>,
    var hits: Int,
    var misses: Int,
) : TrailerSnapshotStore {
    override fun find(code: String): TrailerSnapshot? {
        val hit = (cache.get(code) as TrailerSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerSnapshot> = inner.list()
}

fun newCachedTrailerSnapshotStore(inner: TrailerSnapshotStore, ttl: Int, clock: Clock): TrailerSnapshotStore {
    return CachedTrailerSnapshotStore(
        inner = inner,
        cache = newTtlCache<TrailerSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer snapshots. Empty fields match all. */
data class TrailerSnapshotQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<TrailerSnapshot>): MutableList<TrailerSnapshot> {
        val matched = filter(items) { candidate: TrailerSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerSnapshots(store: TrailerSnapshotStore, query: TrailerSnapshotQuery): MutableList<TrailerSnapshot> {
    return query.apply(store.list())
}
