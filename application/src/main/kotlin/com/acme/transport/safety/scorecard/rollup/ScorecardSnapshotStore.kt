package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard snapshots by code. */
interface ScorecardSnapshotStore {
    fun find(code: String): ScorecardSnapshot?
    fun save(item: ScorecardSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardSnapshotStore(
    var items: MutableMap<String, ScorecardSnapshot>,
    var order: MutableList<String>,
) : ScorecardSnapshotStore {
    override fun find(code: String): ScorecardSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardSnapshot) {
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

    override fun list(): MutableList<ScorecardSnapshot> {
        val out = mutableListOf<ScorecardSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardSnapshotStore(): ScorecardSnapshotStore {
    return MemoryScorecardSnapshotStore(mutableMapOf<String, ScorecardSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardSnapshotStore(
    var inner: ScorecardSnapshotStore,
    var cache: TtlCache<ScorecardSnapshot>,
    var hits: Int,
    var misses: Int,
) : ScorecardSnapshotStore {
    override fun find(code: String): ScorecardSnapshot? {
        val hit = (cache.get(code) as ScorecardSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardSnapshot> = inner.list()
}

fun newCachedScorecardSnapshotStore(inner: ScorecardSnapshotStore, ttl: Int, clock: Clock): ScorecardSnapshotStore {
    return CachedScorecardSnapshotStore(
        inner = inner,
        cache = newTtlCache<ScorecardSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard snapshots. Empty fields match all. */
data class ScorecardSnapshotQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScorecardSnapshot>): MutableList<ScorecardSnapshot> {
        val matched = filter(items) { candidate: ScorecardSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardSnapshots(store: ScorecardSnapshotStore, query: ScorecardSnapshotQuery): MutableList<ScorecardSnapshot> {
    return query.apply(store.list())
}
