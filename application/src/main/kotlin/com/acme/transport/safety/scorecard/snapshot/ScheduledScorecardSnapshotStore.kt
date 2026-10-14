package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled scorecard snapshots by code. */
interface ScheduledScorecardSnapshotStore {
    fun find(code: String): ScheduledScorecardSnapshot?
    fun save(item: ScheduledScorecardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledScorecardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledScorecardSnapshotStore(
    var items: MutableMap<String, ScheduledScorecardSnapshot>,
    var order: MutableList<String>,
) : ScheduledScorecardSnapshotStore {
    override fun find(code: String): ScheduledScorecardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledScorecardSnapshot) {
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

    override fun list(): MutableList<ScheduledScorecardSnapshot> {
        val out = mutableListOf<ScheduledScorecardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledScorecardSnapshotStore(): ScheduledScorecardSnapshotStore {
    return MemoryScheduledScorecardSnapshotStore(
        mutableMapOf<String, ScheduledScorecardSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledScorecardSnapshotStore(
    var inner: ScheduledScorecardSnapshotStore,
    var cache: TtlCache<ScheduledScorecardSnapshot>,
    var hits: Int,
    var misses: Int,
) : ScheduledScorecardSnapshotStore {
    override fun find(code: String): ScheduledScorecardSnapshot? {
        val hit = (cache.get(code) as ScheduledScorecardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledScorecardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledScorecardSnapshot> = inner.list()
}

fun newCachedScheduledScorecardSnapshotStore(inner: ScheduledScorecardSnapshotStore, ttl: Int, clock: Clock): ScheduledScorecardSnapshotStore {
    return CachedScheduledScorecardSnapshotStore(
        inner = inner,
        cache = newTtlCache<ScheduledScorecardSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled scorecard snapshots. Empty fields match all. */
data class ScheduledScorecardSnapshotQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledScorecardSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledScorecardSnapshot>): MutableList<ScheduledScorecardSnapshot> {
        val matched = filter(items) { candidate: ScheduledScorecardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledScorecardSnapshots(store: ScheduledScorecardSnapshotStore, query: ScheduledScorecardSnapshotQuery): MutableList<ScheduledScorecardSnapshot> {
    return query.apply(store.list())
}
