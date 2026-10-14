package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery snapshots by code. */
interface RedeliverySnapshotStore {
    fun find(code: String): RedeliverySnapshot?
    fun save(item: RedeliverySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliverySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliverySnapshotStore(
    var items: MutableMap<String, RedeliverySnapshot>,
    var order: MutableList<String>,
) : RedeliverySnapshotStore {
    override fun find(code: String): RedeliverySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliverySnapshot) {
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

    override fun list(): MutableList<RedeliverySnapshot> {
        val out = mutableListOf<RedeliverySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliverySnapshotStore(): RedeliverySnapshotStore {
    return MemoryRedeliverySnapshotStore(mutableMapOf<String, RedeliverySnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliverySnapshotStore(
    var inner: RedeliverySnapshotStore,
    var cache: TtlCache<RedeliverySnapshot>,
    var hits: Int,
    var misses: Int,
) : RedeliverySnapshotStore {
    override fun find(code: String): RedeliverySnapshot? {
        val hit = (cache.get(code) as RedeliverySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliverySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliverySnapshot> = inner.list()
}

fun newCachedRedeliverySnapshotStore(inner: RedeliverySnapshotStore, ttl: Int, clock: Clock): RedeliverySnapshotStore {
    return CachedRedeliverySnapshotStore(
        inner = inner,
        cache = newTtlCache<RedeliverySnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery snapshots. Empty fields match all. */
data class RedeliverySnapshotQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliverySnapshot): Boolean {
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

    fun apply(items: MutableList<RedeliverySnapshot>): MutableList<RedeliverySnapshot> {
        val matched = filter(items) { candidate: RedeliverySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliverySnapshots(store: RedeliverySnapshotStore, query: RedeliverySnapshotQuery): MutableList<RedeliverySnapshot> {
    return query.apply(store.list())
}
