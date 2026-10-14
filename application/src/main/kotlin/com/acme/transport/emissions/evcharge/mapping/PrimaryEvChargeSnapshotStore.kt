package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary ev charge snapshots by code. */
interface PrimaryEvChargeSnapshotStore {
    fun find(code: String): PrimaryEvChargeSnapshot?
    fun save(item: PrimaryEvChargeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryEvChargeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryEvChargeSnapshotStore(
    var items: MutableMap<String, PrimaryEvChargeSnapshot>,
    var order: MutableList<String>,
) : PrimaryEvChargeSnapshotStore {
    override fun find(code: String): PrimaryEvChargeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryEvChargeSnapshot) {
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

    override fun list(): MutableList<PrimaryEvChargeSnapshot> {
        val out = mutableListOf<PrimaryEvChargeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryEvChargeSnapshotStore(): PrimaryEvChargeSnapshotStore {
    return MemoryPrimaryEvChargeSnapshotStore(
        mutableMapOf<String, PrimaryEvChargeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryEvChargeSnapshotStore(
    var inner: PrimaryEvChargeSnapshotStore,
    var cache: TtlCache<PrimaryEvChargeSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryEvChargeSnapshotStore {
    override fun find(code: String): PrimaryEvChargeSnapshot? {
        val hit = (cache.get(code) as PrimaryEvChargeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryEvChargeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryEvChargeSnapshot> = inner.list()
}

fun newCachedPrimaryEvChargeSnapshotStore(inner: PrimaryEvChargeSnapshotStore, ttl: Int, clock: Clock): PrimaryEvChargeSnapshotStore {
    return CachedPrimaryEvChargeSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryEvChargeSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary ev charge snapshots. Empty fields match all. */
data class PrimaryEvChargeSnapshotQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryEvChargeSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryEvChargeSnapshot>): MutableList<PrimaryEvChargeSnapshot> {
        val matched = filter(items) { candidate: PrimaryEvChargeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryEvChargeSnapshots(store: PrimaryEvChargeSnapshotStore, query: PrimaryEvChargeSnapshotQuery): MutableList<PrimaryEvChargeSnapshot> {
    return query.apply(store.list())
}
