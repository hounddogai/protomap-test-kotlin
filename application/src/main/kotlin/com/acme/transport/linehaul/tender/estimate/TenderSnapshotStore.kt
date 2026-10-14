package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender snapshots by code. */
interface TenderSnapshotStore {
    fun find(code: String): TenderSnapshot?
    fun save(item: TenderSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderSnapshotStore(
    var items: MutableMap<String, TenderSnapshot>,
    var order: MutableList<String>,
) : TenderSnapshotStore {
    override fun find(code: String): TenderSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderSnapshot) {
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

    override fun list(): MutableList<TenderSnapshot> {
        val out = mutableListOf<TenderSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderSnapshotStore(): TenderSnapshotStore {
    return MemoryTenderSnapshotStore(mutableMapOf<String, TenderSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderSnapshotStore(
    var inner: TenderSnapshotStore,
    var cache: TtlCache<TenderSnapshot>,
    var hits: Int,
    var misses: Int,
) : TenderSnapshotStore {
    override fun find(code: String): TenderSnapshot? {
        val hit = (cache.get(code) as TenderSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderSnapshot> = inner.list()
}

fun newCachedTenderSnapshotStore(inner: TenderSnapshotStore, ttl: Int, clock: Clock): TenderSnapshotStore {
    return CachedTenderSnapshotStore(
        inner = inner,
        cache = newTtlCache<TenderSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender snapshots. Empty fields match all. */
data class TenderSnapshotQuery(
    var unit: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderSnapshot>): MutableList<TenderSnapshot> {
        val matched = filter(items) { candidate: TenderSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderSnapshots(store: TenderSnapshotStore, query: TenderSnapshotQuery): MutableList<TenderSnapshot> {
    return query.apply(store.list())
}
