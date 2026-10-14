package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted incident snapshots by code. */
interface WeightedIncidentSnapshotStore {
    fun find(code: String): WeightedIncidentSnapshot?
    fun save(item: WeightedIncidentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedIncidentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedIncidentSnapshotStore(
    var items: MutableMap<String, WeightedIncidentSnapshot>,
    var order: MutableList<String>,
) : WeightedIncidentSnapshotStore {
    override fun find(code: String): WeightedIncidentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedIncidentSnapshot) {
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

    override fun list(): MutableList<WeightedIncidentSnapshot> {
        val out = mutableListOf<WeightedIncidentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedIncidentSnapshotStore(): WeightedIncidentSnapshotStore {
    return MemoryWeightedIncidentSnapshotStore(
        mutableMapOf<String, WeightedIncidentSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedIncidentSnapshotStore(
    var inner: WeightedIncidentSnapshotStore,
    var cache: TtlCache<WeightedIncidentSnapshot>,
    var hits: Int,
    var misses: Int,
) : WeightedIncidentSnapshotStore {
    override fun find(code: String): WeightedIncidentSnapshot? {
        val hit = (cache.get(code) as WeightedIncidentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedIncidentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedIncidentSnapshot> = inner.list()
}

fun newCachedWeightedIncidentSnapshotStore(inner: WeightedIncidentSnapshotStore, ttl: Int, clock: Clock): WeightedIncidentSnapshotStore {
    return CachedWeightedIncidentSnapshotStore(
        inner = inner,
        cache = newTtlCache<WeightedIncidentSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted incident snapshots. Empty fields match all. */
data class WeightedIncidentSnapshotQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedIncidentSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedIncidentSnapshot>): MutableList<WeightedIncidentSnapshot> {
        val matched = filter(items) { candidate: WeightedIncidentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedIncidentSnapshots(store: WeightedIncidentSnapshotStore, query: WeightedIncidentSnapshotQuery): MutableList<WeightedIncidentSnapshot> {
    return query.apply(store.list())
}
