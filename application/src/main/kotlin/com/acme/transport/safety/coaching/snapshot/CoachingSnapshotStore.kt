package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching snapshots by code. */
interface CoachingSnapshotStore {
    fun find(code: String): CoachingSnapshot?
    fun save(item: CoachingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingSnapshotStore(
    var items: MutableMap<String, CoachingSnapshot>,
    var order: MutableList<String>,
) : CoachingSnapshotStore {
    override fun find(code: String): CoachingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingSnapshot) {
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

    override fun list(): MutableList<CoachingSnapshot> {
        val out = mutableListOf<CoachingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingSnapshotStore(): CoachingSnapshotStore {
    return MemoryCoachingSnapshotStore(mutableMapOf<String, CoachingSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingSnapshotStore(
    var inner: CoachingSnapshotStore,
    var cache: TtlCache<CoachingSnapshot>,
    var hits: Int,
    var misses: Int,
) : CoachingSnapshotStore {
    override fun find(code: String): CoachingSnapshot? {
        val hit = (cache.get(code) as CoachingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingSnapshot> = inner.list()
}

fun newCachedCoachingSnapshotStore(inner: CoachingSnapshotStore, ttl: Int, clock: Clock): CoachingSnapshotStore {
    return CachedCoachingSnapshotStore(
        inner = inner,
        cache = newTtlCache<CoachingSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching snapshots. Empty fields match all. */
data class CoachingSnapshotQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingSnapshot>): MutableList<CoachingSnapshot> {
        val matched = filter(items) { candidate: CoachingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingSnapshots(store: CoachingSnapshotStore, query: CoachingSnapshotQuery): MutableList<CoachingSnapshot> {
    return query.apply(store.list())
}
