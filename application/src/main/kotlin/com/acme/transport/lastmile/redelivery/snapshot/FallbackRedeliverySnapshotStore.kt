package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback redelivery snapshots by code. */
interface FallbackRedeliverySnapshotStore {
    fun find(code: String): FallbackRedeliverySnapshot?
    fun save(item: FallbackRedeliverySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRedeliverySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRedeliverySnapshotStore(
    var items: MutableMap<String, FallbackRedeliverySnapshot>,
    var order: MutableList<String>,
) : FallbackRedeliverySnapshotStore {
    override fun find(code: String): FallbackRedeliverySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRedeliverySnapshot) {
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

    override fun list(): MutableList<FallbackRedeliverySnapshot> {
        val out = mutableListOf<FallbackRedeliverySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRedeliverySnapshotStore(): FallbackRedeliverySnapshotStore {
    return MemoryFallbackRedeliverySnapshotStore(
        mutableMapOf<String, FallbackRedeliverySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRedeliverySnapshotStore(
    var inner: FallbackRedeliverySnapshotStore,
    var cache: TtlCache<FallbackRedeliverySnapshot>,
    var hits: Int,
    var misses: Int,
) : FallbackRedeliverySnapshotStore {
    override fun find(code: String): FallbackRedeliverySnapshot? {
        val hit = (cache.get(code) as FallbackRedeliverySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRedeliverySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRedeliverySnapshot> = inner.list()
}

fun newCachedFallbackRedeliverySnapshotStore(inner: FallbackRedeliverySnapshotStore, ttl: Int, clock: Clock): FallbackRedeliverySnapshotStore {
    return CachedFallbackRedeliverySnapshotStore(
        inner = inner,
        cache = newTtlCache<FallbackRedeliverySnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback redelivery snapshots. Empty fields match all. */
data class FallbackRedeliverySnapshotQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRedeliverySnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<FallbackRedeliverySnapshot>): MutableList<FallbackRedeliverySnapshot> {
        val matched = filter(items) { candidate: FallbackRedeliverySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRedeliverySnapshots(store: FallbackRedeliverySnapshotStore, query: FallbackRedeliverySnapshotQuery): MutableList<FallbackRedeliverySnapshot> {
    return query.apply(store.list())
}
