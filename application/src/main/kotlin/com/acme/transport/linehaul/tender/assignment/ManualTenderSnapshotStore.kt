package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual tender snapshots by code. */
interface ManualTenderSnapshotStore {
    fun find(code: String): ManualTenderSnapshot?
    fun save(item: ManualTenderSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTenderSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTenderSnapshotStore(
    var items: MutableMap<String, ManualTenderSnapshot>,
    var order: MutableList<String>,
) : ManualTenderSnapshotStore {
    override fun find(code: String): ManualTenderSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTenderSnapshot) {
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

    override fun list(): MutableList<ManualTenderSnapshot> {
        val out = mutableListOf<ManualTenderSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTenderSnapshotStore(): ManualTenderSnapshotStore {
    return MemoryManualTenderSnapshotStore(
        mutableMapOf<String, ManualTenderSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTenderSnapshotStore(
    var inner: ManualTenderSnapshotStore,
    var cache: TtlCache<ManualTenderSnapshot>,
    var hits: Int,
    var misses: Int,
) : ManualTenderSnapshotStore {
    override fun find(code: String): ManualTenderSnapshot? {
        val hit = (cache.get(code) as ManualTenderSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTenderSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTenderSnapshot> = inner.list()
}

fun newCachedManualTenderSnapshotStore(inner: ManualTenderSnapshotStore, ttl: Int, clock: Clock): ManualTenderSnapshotStore {
    return CachedManualTenderSnapshotStore(
        inner = inner,
        cache = newTtlCache<ManualTenderSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tender snapshots. Empty fields match all. */
data class ManualTenderSnapshotQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTenderSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTenderSnapshot>): MutableList<ManualTenderSnapshot> {
        val matched = filter(items) { candidate: ManualTenderSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTenderSnapshots(store: ManualTenderSnapshotStore, query: ManualTenderSnapshotQuery): MutableList<ManualTenderSnapshot> {
    return query.apply(store.list())
}
