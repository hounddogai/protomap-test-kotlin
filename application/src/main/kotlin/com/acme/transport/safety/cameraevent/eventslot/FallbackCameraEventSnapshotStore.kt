package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback camera event snapshots by code. */
interface FallbackCameraEventSnapshotStore {
    fun find(code: String): FallbackCameraEventSnapshot?
    fun save(item: FallbackCameraEventSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCameraEventSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCameraEventSnapshotStore(
    var items: MutableMap<String, FallbackCameraEventSnapshot>,
    var order: MutableList<String>,
) : FallbackCameraEventSnapshotStore {
    override fun find(code: String): FallbackCameraEventSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCameraEventSnapshot) {
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

    override fun list(): MutableList<FallbackCameraEventSnapshot> {
        val out = mutableListOf<FallbackCameraEventSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCameraEventSnapshotStore(): FallbackCameraEventSnapshotStore {
    return MemoryFallbackCameraEventSnapshotStore(
        mutableMapOf<String, FallbackCameraEventSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCameraEventSnapshotStore(
    var inner: FallbackCameraEventSnapshotStore,
    var cache: TtlCache<FallbackCameraEventSnapshot>,
    var hits: Int,
    var misses: Int,
) : FallbackCameraEventSnapshotStore {
    override fun find(code: String): FallbackCameraEventSnapshot? {
        val hit = (cache.get(code) as FallbackCameraEventSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCameraEventSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCameraEventSnapshot> = inner.list()
}

fun newCachedFallbackCameraEventSnapshotStore(inner: FallbackCameraEventSnapshotStore, ttl: Int, clock: Clock): FallbackCameraEventSnapshotStore {
    return CachedFallbackCameraEventSnapshotStore(
        inner = inner,
        cache = newTtlCache<FallbackCameraEventSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback camera event snapshots. Empty fields match all. */
data class FallbackCameraEventSnapshotQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCameraEventSnapshot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<FallbackCameraEventSnapshot>): MutableList<FallbackCameraEventSnapshot> {
        val matched = filter(items) { candidate: FallbackCameraEventSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCameraEventSnapshots(store: FallbackCameraEventSnapshotStore, query: FallbackCameraEventSnapshotQuery): MutableList<FallbackCameraEventSnapshot> {
    return query.apply(store.list())
}
