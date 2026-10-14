package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient scorecard snapshots by code. */
interface LenientScorecardSnapshotStore {
    fun find(code: String): LenientScorecardSnapshot?
    fun save(item: LenientScorecardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientScorecardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientScorecardSnapshotStore(
    var items: MutableMap<String, LenientScorecardSnapshot>,
    var order: MutableList<String>,
) : LenientScorecardSnapshotStore {
    override fun find(code: String): LenientScorecardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientScorecardSnapshot) {
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

    override fun list(): MutableList<LenientScorecardSnapshot> {
        val out = mutableListOf<LenientScorecardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientScorecardSnapshotStore(): LenientScorecardSnapshotStore {
    return MemoryLenientScorecardSnapshotStore(
        mutableMapOf<String, LenientScorecardSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientScorecardSnapshotStore(
    var inner: LenientScorecardSnapshotStore,
    var cache: TtlCache<LenientScorecardSnapshot>,
    var hits: Int,
    var misses: Int,
) : LenientScorecardSnapshotStore {
    override fun find(code: String): LenientScorecardSnapshot? {
        val hit = (cache.get(code) as LenientScorecardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientScorecardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientScorecardSnapshot> = inner.list()
}

fun newCachedLenientScorecardSnapshotStore(inner: LenientScorecardSnapshotStore, ttl: Int, clock: Clock): LenientScorecardSnapshotStore {
    return CachedLenientScorecardSnapshotStore(
        inner = inner,
        cache = newTtlCache<LenientScorecardSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient scorecard snapshots. Empty fields match all. */
data class LenientScorecardSnapshotQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientScorecardSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientScorecardSnapshot>): MutableList<LenientScorecardSnapshot> {
        val matched = filter(items) { candidate: LenientScorecardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientScorecardSnapshots(store: LenientScorecardSnapshotStore, query: LenientScorecardSnapshotQuery): MutableList<LenientScorecardSnapshot> {
    return query.apply(store.list())
}
