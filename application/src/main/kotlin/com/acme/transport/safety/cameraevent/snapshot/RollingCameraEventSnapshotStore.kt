package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling camera event snapshots by code. */
interface RollingCameraEventSnapshotStore {
    fun find(code: String): RollingCameraEventSnapshot?
    fun save(item: RollingCameraEventSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCameraEventSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCameraEventSnapshotStore(
    var items: MutableMap<String, RollingCameraEventSnapshot>,
    var order: MutableList<String>,
) : RollingCameraEventSnapshotStore {
    override fun find(code: String): RollingCameraEventSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCameraEventSnapshot) {
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

    override fun list(): MutableList<RollingCameraEventSnapshot> {
        val out = mutableListOf<RollingCameraEventSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCameraEventSnapshotStore(): RollingCameraEventSnapshotStore {
    return MemoryRollingCameraEventSnapshotStore(
        mutableMapOf<String, RollingCameraEventSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCameraEventSnapshotStore(
    var inner: RollingCameraEventSnapshotStore,
    var cache: TtlCache<RollingCameraEventSnapshot>,
    var hits: Int,
    var misses: Int,
) : RollingCameraEventSnapshotStore {
    override fun find(code: String): RollingCameraEventSnapshot? {
        val hit = (cache.get(code) as RollingCameraEventSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCameraEventSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCameraEventSnapshot> = inner.list()
}

fun newCachedRollingCameraEventSnapshotStore(inner: RollingCameraEventSnapshotStore, ttl: Int, clock: Clock): RollingCameraEventSnapshotStore {
    return CachedRollingCameraEventSnapshotStore(
        inner = inner,
        cache = newTtlCache<RollingCameraEventSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling camera event snapshots. Empty fields match all. */
data class RollingCameraEventSnapshotQuery(
    var region: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCameraEventSnapshot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCameraEventSnapshot>): MutableList<RollingCameraEventSnapshot> {
        val matched = filter(items) { candidate: RollingCameraEventSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCameraEventSnapshots(store: RollingCameraEventSnapshotStore, query: RollingCameraEventSnapshotQuery): MutableList<RollingCameraEventSnapshot> {
    return query.apply(store.list())
}
