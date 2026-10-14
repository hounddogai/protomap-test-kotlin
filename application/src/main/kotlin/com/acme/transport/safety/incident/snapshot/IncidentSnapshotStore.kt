package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident snapshots by code. */
interface IncidentSnapshotStore {
    fun find(code: String): IncidentSnapshot?
    fun save(item: IncidentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentSnapshotStore(
    var items: MutableMap<String, IncidentSnapshot>,
    var order: MutableList<String>,
) : IncidentSnapshotStore {
    override fun find(code: String): IncidentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentSnapshot) {
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

    override fun list(): MutableList<IncidentSnapshot> {
        val out = mutableListOf<IncidentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentSnapshotStore(): IncidentSnapshotStore {
    return MemoryIncidentSnapshotStore(mutableMapOf<String, IncidentSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentSnapshotStore(
    var inner: IncidentSnapshotStore,
    var cache: TtlCache<IncidentSnapshot>,
    var hits: Int,
    var misses: Int,
) : IncidentSnapshotStore {
    override fun find(code: String): IncidentSnapshot? {
        val hit = (cache.get(code) as IncidentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentSnapshot> = inner.list()
}

fun newCachedIncidentSnapshotStore(inner: IncidentSnapshotStore, ttl: Int, clock: Clock): IncidentSnapshotStore {
    return CachedIncidentSnapshotStore(
        inner = inner,
        cache = newTtlCache<IncidentSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident snapshots. Empty fields match all. */
data class IncidentSnapshotQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentSnapshot>): MutableList<IncidentSnapshot> {
        val matched = filter(items) { candidate: IncidentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentSnapshots(store: IncidentSnapshotStore, query: IncidentSnapshotQuery): MutableList<IncidentSnapshot> {
    return query.apply(store.list())
}
