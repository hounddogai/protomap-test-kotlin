package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy coaching snapshots by code. */
interface LegacyCoachingSnapshotStore {
    fun find(code: String): LegacyCoachingSnapshot?
    fun save(item: LegacyCoachingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCoachingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCoachingSnapshotStore(
    var items: MutableMap<String, LegacyCoachingSnapshot>,
    var order: MutableList<String>,
) : LegacyCoachingSnapshotStore {
    override fun find(code: String): LegacyCoachingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCoachingSnapshot) {
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

    override fun list(): MutableList<LegacyCoachingSnapshot> {
        val out = mutableListOf<LegacyCoachingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCoachingSnapshotStore(): LegacyCoachingSnapshotStore {
    return MemoryLegacyCoachingSnapshotStore(
        mutableMapOf<String, LegacyCoachingSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCoachingSnapshotStore(
    var inner: LegacyCoachingSnapshotStore,
    var cache: TtlCache<LegacyCoachingSnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacyCoachingSnapshotStore {
    override fun find(code: String): LegacyCoachingSnapshot? {
        val hit = (cache.get(code) as LegacyCoachingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCoachingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCoachingSnapshot> = inner.list()
}

fun newCachedLegacyCoachingSnapshotStore(inner: LegacyCoachingSnapshotStore, ttl: Int, clock: Clock): LegacyCoachingSnapshotStore {
    return CachedLegacyCoachingSnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacyCoachingSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy coaching snapshots. Empty fields match all. */
data class LegacyCoachingSnapshotQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCoachingSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyCoachingSnapshot>): MutableList<LegacyCoachingSnapshot> {
        val matched = filter(items) { candidate: LegacyCoachingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCoachingSnapshots(store: LegacyCoachingSnapshotStore, query: LegacyCoachingSnapshotQuery): MutableList<LegacyCoachingSnapshot> {
    return query.apply(store.list())
}
