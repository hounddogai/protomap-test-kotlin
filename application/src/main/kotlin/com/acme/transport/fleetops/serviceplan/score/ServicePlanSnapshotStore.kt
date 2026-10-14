package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan snapshots by code. */
interface ServicePlanSnapshotStore {
    fun find(code: String): ServicePlanSnapshot?
    fun save(item: ServicePlanSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanSnapshotStore(
    var items: MutableMap<String, ServicePlanSnapshot>,
    var order: MutableList<String>,
) : ServicePlanSnapshotStore {
    override fun find(code: String): ServicePlanSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanSnapshot) {
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

    override fun list(): MutableList<ServicePlanSnapshot> {
        val out = mutableListOf<ServicePlanSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanSnapshotStore(): ServicePlanSnapshotStore {
    return MemoryServicePlanSnapshotStore(mutableMapOf<String, ServicePlanSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanSnapshotStore(
    var inner: ServicePlanSnapshotStore,
    var cache: TtlCache<ServicePlanSnapshot>,
    var hits: Int,
    var misses: Int,
) : ServicePlanSnapshotStore {
    override fun find(code: String): ServicePlanSnapshot? {
        val hit = (cache.get(code) as ServicePlanSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanSnapshot> = inner.list()
}

fun newCachedServicePlanSnapshotStore(inner: ServicePlanSnapshotStore, ttl: Int, clock: Clock): ServicePlanSnapshotStore {
    return CachedServicePlanSnapshotStore(
        inner = inner,
        cache = newTtlCache<ServicePlanSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan snapshots. Empty fields match all. */
data class ServicePlanSnapshotQuery(
    var lane: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanSnapshot>): MutableList<ServicePlanSnapshot> {
        val matched = filter(items) { candidate: ServicePlanSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanSnapshots(store: ServicePlanSnapshotStore, query: ServicePlanSnapshotQuery): MutableList<ServicePlanSnapshot> {
    return query.apply(store.list())
}
