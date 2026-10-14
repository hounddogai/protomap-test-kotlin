package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker snapshots by code. */
interface BrokerSnapshotStore {
    fun find(code: String): BrokerSnapshot?
    fun save(item: BrokerSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerSnapshotStore(
    var items: MutableMap<String, BrokerSnapshot>,
    var order: MutableList<String>,
) : BrokerSnapshotStore {
    override fun find(code: String): BrokerSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerSnapshot) {
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

    override fun list(): MutableList<BrokerSnapshot> {
        val out = mutableListOf<BrokerSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerSnapshotStore(): BrokerSnapshotStore {
    return MemoryBrokerSnapshotStore(mutableMapOf<String, BrokerSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerSnapshotStore(
    var inner: BrokerSnapshotStore,
    var cache: TtlCache<BrokerSnapshot>,
    var hits: Int,
    var misses: Int,
) : BrokerSnapshotStore {
    override fun find(code: String): BrokerSnapshot? {
        val hit = (cache.get(code) as BrokerSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerSnapshot> = inner.list()
}

fun newCachedBrokerSnapshotStore(inner: BrokerSnapshotStore, ttl: Int, clock: Clock): BrokerSnapshotStore {
    return CachedBrokerSnapshotStore(
        inner = inner,
        cache = newTtlCache<BrokerSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker snapshots. Empty fields match all. */
data class BrokerSnapshotQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerSnapshot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerSnapshot>): MutableList<BrokerSnapshot> {
        val matched = filter(items) { candidate: BrokerSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerSnapshots(store: BrokerSnapshotStore, query: BrokerSnapshotQuery): MutableList<BrokerSnapshot> {
    return query.apply(store.list())
}
