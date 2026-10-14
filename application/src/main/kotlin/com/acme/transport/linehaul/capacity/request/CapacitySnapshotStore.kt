package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity snapshots by code. */
interface CapacitySnapshotStore {
    fun find(code: String): CapacitySnapshot?
    fun save(item: CapacitySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacitySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacitySnapshotStore(
    var items: MutableMap<String, CapacitySnapshot>,
    var order: MutableList<String>,
) : CapacitySnapshotStore {
    override fun find(code: String): CapacitySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacitySnapshot) {
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

    override fun list(): MutableList<CapacitySnapshot> {
        val out = mutableListOf<CapacitySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacitySnapshotStore(): CapacitySnapshotStore {
    return MemoryCapacitySnapshotStore(mutableMapOf<String, CapacitySnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacitySnapshotStore(
    var inner: CapacitySnapshotStore,
    var cache: TtlCache<CapacitySnapshot>,
    var hits: Int,
    var misses: Int,
) : CapacitySnapshotStore {
    override fun find(code: String): CapacitySnapshot? {
        val hit = (cache.get(code) as CapacitySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacitySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacitySnapshot> = inner.list()
}

fun newCachedCapacitySnapshotStore(inner: CapacitySnapshotStore, ttl: Int, clock: Clock): CapacitySnapshotStore {
    return CachedCapacitySnapshotStore(
        inner = inner,
        cache = newTtlCache<CapacitySnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity snapshots. Empty fields match all. */
data class CapacitySnapshotQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacitySnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacitySnapshot>): MutableList<CapacitySnapshot> {
        val matched = filter(items) { candidate: CapacitySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacitySnapshots(store: CapacitySnapshotStore, query: CapacitySnapshotQuery): MutableList<CapacitySnapshot> {
    return query.apply(store.list())
}
