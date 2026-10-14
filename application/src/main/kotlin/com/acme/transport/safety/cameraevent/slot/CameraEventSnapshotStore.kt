package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event snapshots by code. */
interface CameraEventSnapshotStore {
    fun find(code: String): CameraEventSnapshot?
    fun save(item: CameraEventSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventSnapshotStore(
    var items: MutableMap<String, CameraEventSnapshot>,
    var order: MutableList<String>,
) : CameraEventSnapshotStore {
    override fun find(code: String): CameraEventSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventSnapshot) {
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

    override fun list(): MutableList<CameraEventSnapshot> {
        val out = mutableListOf<CameraEventSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventSnapshotStore(): CameraEventSnapshotStore {
    return MemoryCameraEventSnapshotStore(mutableMapOf<String, CameraEventSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventSnapshotStore(
    var inner: CameraEventSnapshotStore,
    var cache: TtlCache<CameraEventSnapshot>,
    var hits: Int,
    var misses: Int,
) : CameraEventSnapshotStore {
    override fun find(code: String): CameraEventSnapshot? {
        val hit = (cache.get(code) as CameraEventSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventSnapshot> = inner.list()
}

fun newCachedCameraEventSnapshotStore(inner: CameraEventSnapshotStore, ttl: Int, clock: Clock): CameraEventSnapshotStore {
    return CachedCameraEventSnapshotStore(
        inner = inner,
        cache = newTtlCache<CameraEventSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event snapshots. Empty fields match all. */
data class CameraEventSnapshotQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventSnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventSnapshot>): MutableList<CameraEventSnapshot> {
        val matched = filter(items) { candidate: CameraEventSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventSnapshots(store: CameraEventSnapshotStore, query: CameraEventSnapshotQuery): MutableList<CameraEventSnapshot> {
    return query.apply(store.list())
}
