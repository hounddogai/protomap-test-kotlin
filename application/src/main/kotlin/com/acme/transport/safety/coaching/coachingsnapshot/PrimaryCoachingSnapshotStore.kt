package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary coaching snapshots by code. */
interface PrimaryCoachingSnapshotStore {
    fun find(code: String): PrimaryCoachingSnapshot?
    fun save(item: PrimaryCoachingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCoachingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCoachingSnapshotStore(
    var items: MutableMap<String, PrimaryCoachingSnapshot>,
    var order: MutableList<String>,
) : PrimaryCoachingSnapshotStore {
    override fun find(code: String): PrimaryCoachingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCoachingSnapshot) {
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

    override fun list(): MutableList<PrimaryCoachingSnapshot> {
        val out = mutableListOf<PrimaryCoachingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCoachingSnapshotStore(): PrimaryCoachingSnapshotStore {
    return MemoryPrimaryCoachingSnapshotStore(
        mutableMapOf<String, PrimaryCoachingSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCoachingSnapshotStore(
    var inner: PrimaryCoachingSnapshotStore,
    var cache: TtlCache<PrimaryCoachingSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryCoachingSnapshotStore {
    override fun find(code: String): PrimaryCoachingSnapshot? {
        val hit = (cache.get(code) as PrimaryCoachingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCoachingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCoachingSnapshot> = inner.list()
}

fun newCachedPrimaryCoachingSnapshotStore(inner: PrimaryCoachingSnapshotStore, ttl: Int, clock: Clock): PrimaryCoachingSnapshotStore {
    return CachedPrimaryCoachingSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryCoachingSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary coaching snapshots. Empty fields match all. */
data class PrimaryCoachingSnapshotQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCoachingSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryCoachingSnapshot>): MutableList<PrimaryCoachingSnapshot> {
        val matched = filter(items) { candidate: PrimaryCoachingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCoachingSnapshots(store: PrimaryCoachingSnapshotStore, query: PrimaryCoachingSnapshotQuery): MutableList<PrimaryCoachingSnapshot> {
    return query.apply(store.list())
}
