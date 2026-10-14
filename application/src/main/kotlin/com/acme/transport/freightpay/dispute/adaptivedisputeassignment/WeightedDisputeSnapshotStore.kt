package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted dispute snapshots by code. */
interface WeightedDisputeSnapshotStore {
    fun find(code: String): WeightedDisputeSnapshot?
    fun save(item: WeightedDisputeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDisputeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDisputeSnapshotStore(
    var items: MutableMap<String, WeightedDisputeSnapshot>,
    var order: MutableList<String>,
) : WeightedDisputeSnapshotStore {
    override fun find(code: String): WeightedDisputeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDisputeSnapshot) {
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

    override fun list(): MutableList<WeightedDisputeSnapshot> {
        val out = mutableListOf<WeightedDisputeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDisputeSnapshotStore(): WeightedDisputeSnapshotStore {
    return MemoryWeightedDisputeSnapshotStore(
        mutableMapOf<String, WeightedDisputeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDisputeSnapshotStore(
    var inner: WeightedDisputeSnapshotStore,
    var cache: TtlCache<WeightedDisputeSnapshot>,
    var hits: Int,
    var misses: Int,
) : WeightedDisputeSnapshotStore {
    override fun find(code: String): WeightedDisputeSnapshot? {
        val hit = (cache.get(code) as WeightedDisputeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDisputeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDisputeSnapshot> = inner.list()
}

fun newCachedWeightedDisputeSnapshotStore(inner: WeightedDisputeSnapshotStore, ttl: Int, clock: Clock): WeightedDisputeSnapshotStore {
    return CachedWeightedDisputeSnapshotStore(
        inner = inner,
        cache = newTtlCache<WeightedDisputeSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dispute snapshots. Empty fields match all. */
data class WeightedDisputeSnapshotQuery(
    var channel: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDisputeSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDisputeSnapshot>): MutableList<WeightedDisputeSnapshot> {
        val matched = filter(items) { candidate: WeightedDisputeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDisputeSnapshots(store: WeightedDisputeSnapshotStore, query: WeightedDisputeSnapshotQuery): MutableList<WeightedDisputeSnapshot> {
    return query.apply(store.list())
}
