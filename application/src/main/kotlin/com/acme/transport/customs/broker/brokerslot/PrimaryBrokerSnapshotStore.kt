package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary broker snapshots by code. */
interface PrimaryBrokerSnapshotStore {
    fun find(code: String): PrimaryBrokerSnapshot?
    fun save(item: PrimaryBrokerSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBrokerSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBrokerSnapshotStore(
    var items: MutableMap<String, PrimaryBrokerSnapshot>,
    var order: MutableList<String>,
) : PrimaryBrokerSnapshotStore {
    override fun find(code: String): PrimaryBrokerSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBrokerSnapshot) {
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

    override fun list(): MutableList<PrimaryBrokerSnapshot> {
        val out = mutableListOf<PrimaryBrokerSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBrokerSnapshotStore(): PrimaryBrokerSnapshotStore {
    return MemoryPrimaryBrokerSnapshotStore(
        mutableMapOf<String, PrimaryBrokerSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBrokerSnapshotStore(
    var inner: PrimaryBrokerSnapshotStore,
    var cache: TtlCache<PrimaryBrokerSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryBrokerSnapshotStore {
    override fun find(code: String): PrimaryBrokerSnapshot? {
        val hit = (cache.get(code) as PrimaryBrokerSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBrokerSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBrokerSnapshot> = inner.list()
}

fun newCachedPrimaryBrokerSnapshotStore(inner: PrimaryBrokerSnapshotStore, ttl: Int, clock: Clock): PrimaryBrokerSnapshotStore {
    return CachedPrimaryBrokerSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryBrokerSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary broker snapshots. Empty fields match all. */
data class PrimaryBrokerSnapshotQuery(
    var segment: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBrokerSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryBrokerSnapshot>): MutableList<PrimaryBrokerSnapshot> {
        val matched = filter(items) { candidate: PrimaryBrokerSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBrokerSnapshots(store: PrimaryBrokerSnapshotStore, query: PrimaryBrokerSnapshotQuery): MutableList<PrimaryBrokerSnapshot> {
    return query.apply(store.list())
}
