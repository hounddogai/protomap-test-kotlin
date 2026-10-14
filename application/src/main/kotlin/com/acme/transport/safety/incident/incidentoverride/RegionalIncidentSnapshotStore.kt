package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional incident snapshots by code. */
interface RegionalIncidentSnapshotStore {
    fun find(code: String): RegionalIncidentSnapshot?
    fun save(item: RegionalIncidentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIncidentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIncidentSnapshotStore(
    var items: MutableMap<String, RegionalIncidentSnapshot>,
    var order: MutableList<String>,
) : RegionalIncidentSnapshotStore {
    override fun find(code: String): RegionalIncidentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIncidentSnapshot) {
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

    override fun list(): MutableList<RegionalIncidentSnapshot> {
        val out = mutableListOf<RegionalIncidentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIncidentSnapshotStore(): RegionalIncidentSnapshotStore {
    return MemoryRegionalIncidentSnapshotStore(
        mutableMapOf<String, RegionalIncidentSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIncidentSnapshotStore(
    var inner: RegionalIncidentSnapshotStore,
    var cache: TtlCache<RegionalIncidentSnapshot>,
    var hits: Int,
    var misses: Int,
) : RegionalIncidentSnapshotStore {
    override fun find(code: String): RegionalIncidentSnapshot? {
        val hit = (cache.get(code) as RegionalIncidentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIncidentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIncidentSnapshot> = inner.list()
}

fun newCachedRegionalIncidentSnapshotStore(inner: RegionalIncidentSnapshotStore, ttl: Int, clock: Clock): RegionalIncidentSnapshotStore {
    return CachedRegionalIncidentSnapshotStore(
        inner = inner,
        cache = newTtlCache<RegionalIncidentSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional incident snapshots. Empty fields match all. */
data class RegionalIncidentSnapshotQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIncidentSnapshot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIncidentSnapshot>): MutableList<RegionalIncidentSnapshot> {
        val matched = filter(items) { candidate: RegionalIncidentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIncidentSnapshots(store: RegionalIncidentSnapshotStore, query: RegionalIncidentSnapshotQuery): MutableList<RegionalIncidentSnapshot> {
    return query.apply(store.list())
}
