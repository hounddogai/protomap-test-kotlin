package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict incident snapshots by code. */
interface StrictIncidentSnapshotStore {
    fun find(code: String): StrictIncidentSnapshot?
    fun save(item: StrictIncidentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIncidentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIncidentSnapshotStore(
    var items: MutableMap<String, StrictIncidentSnapshot>,
    var order: MutableList<String>,
) : StrictIncidentSnapshotStore {
    override fun find(code: String): StrictIncidentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIncidentSnapshot) {
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

    override fun list(): MutableList<StrictIncidentSnapshot> {
        val out = mutableListOf<StrictIncidentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIncidentSnapshotStore(): StrictIncidentSnapshotStore {
    return MemoryStrictIncidentSnapshotStore(
        mutableMapOf<String, StrictIncidentSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIncidentSnapshotStore(
    var inner: StrictIncidentSnapshotStore,
    var cache: TtlCache<StrictIncidentSnapshot>,
    var hits: Int,
    var misses: Int,
) : StrictIncidentSnapshotStore {
    override fun find(code: String): StrictIncidentSnapshot? {
        val hit = (cache.get(code) as StrictIncidentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIncidentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIncidentSnapshot> = inner.list()
}

fun newCachedStrictIncidentSnapshotStore(inner: StrictIncidentSnapshotStore, ttl: Int, clock: Clock): StrictIncidentSnapshotStore {
    return CachedStrictIncidentSnapshotStore(
        inner = inner,
        cache = newTtlCache<StrictIncidentSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict incident snapshots. Empty fields match all. */
data class StrictIncidentSnapshotQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIncidentSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictIncidentSnapshot>): MutableList<StrictIncidentSnapshot> {
        val matched = filter(items) { candidate: StrictIncidentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIncidentSnapshots(store: StrictIncidentSnapshotStore, query: StrictIncidentSnapshotQuery): MutableList<StrictIncidentSnapshot> {
    return query.apply(store.list())
}
