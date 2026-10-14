package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict idling snapshots by code. */
interface StrictIdlingSnapshotStore {
    fun find(code: String): StrictIdlingSnapshot?
    fun save(item: StrictIdlingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIdlingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIdlingSnapshotStore(
    var items: MutableMap<String, StrictIdlingSnapshot>,
    var order: MutableList<String>,
) : StrictIdlingSnapshotStore {
    override fun find(code: String): StrictIdlingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIdlingSnapshot) {
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

    override fun list(): MutableList<StrictIdlingSnapshot> {
        val out = mutableListOf<StrictIdlingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIdlingSnapshotStore(): StrictIdlingSnapshotStore {
    return MemoryStrictIdlingSnapshotStore(
        mutableMapOf<String, StrictIdlingSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIdlingSnapshotStore(
    var inner: StrictIdlingSnapshotStore,
    var cache: TtlCache<StrictIdlingSnapshot>,
    var hits: Int,
    var misses: Int,
) : StrictIdlingSnapshotStore {
    override fun find(code: String): StrictIdlingSnapshot? {
        val hit = (cache.get(code) as StrictIdlingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIdlingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIdlingSnapshot> = inner.list()
}

fun newCachedStrictIdlingSnapshotStore(inner: StrictIdlingSnapshotStore, ttl: Int, clock: Clock): StrictIdlingSnapshotStore {
    return CachedStrictIdlingSnapshotStore(
        inner = inner,
        cache = newTtlCache<StrictIdlingSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict idling snapshots. Empty fields match all. */
data class StrictIdlingSnapshotQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIdlingSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<StrictIdlingSnapshot>): MutableList<StrictIdlingSnapshot> {
        val matched = filter(items) { candidate: StrictIdlingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIdlingSnapshots(store: StrictIdlingSnapshotStore, query: StrictIdlingSnapshotQuery): MutableList<StrictIdlingSnapshot> {
    return query.apply(store.list())
}
