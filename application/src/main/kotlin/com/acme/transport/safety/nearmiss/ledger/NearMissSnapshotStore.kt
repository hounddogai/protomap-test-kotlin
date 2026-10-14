package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss snapshots by code. */
interface NearMissSnapshotStore {
    fun find(code: String): NearMissSnapshot?
    fun save(item: NearMissSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissSnapshotStore(
    var items: MutableMap<String, NearMissSnapshot>,
    var order: MutableList<String>,
) : NearMissSnapshotStore {
    override fun find(code: String): NearMissSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissSnapshot) {
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

    override fun list(): MutableList<NearMissSnapshot> {
        val out = mutableListOf<NearMissSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissSnapshotStore(): NearMissSnapshotStore {
    return MemoryNearMissSnapshotStore(mutableMapOf<String, NearMissSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissSnapshotStore(
    var inner: NearMissSnapshotStore,
    var cache: TtlCache<NearMissSnapshot>,
    var hits: Int,
    var misses: Int,
) : NearMissSnapshotStore {
    override fun find(code: String): NearMissSnapshot? {
        val hit = (cache.get(code) as NearMissSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissSnapshot> = inner.list()
}

fun newCachedNearMissSnapshotStore(inner: NearMissSnapshotStore, ttl: Int, clock: Clock): NearMissSnapshotStore {
    return CachedNearMissSnapshotStore(
        inner = inner,
        cache = newTtlCache<NearMissSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss snapshots. Empty fields match all. */
data class NearMissSnapshotQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissSnapshot>): MutableList<NearMissSnapshot> {
        val matched = filter(items) { candidate: NearMissSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissSnapshots(store: NearMissSnapshotStore, query: NearMissSnapshotQuery): MutableList<NearMissSnapshot> {
    return query.apply(store.list())
}
