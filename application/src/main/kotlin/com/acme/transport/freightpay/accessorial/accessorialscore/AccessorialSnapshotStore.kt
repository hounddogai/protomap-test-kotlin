package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial snapshots by code. */
interface AccessorialSnapshotStore {
    fun find(code: String): AccessorialSnapshot?
    fun save(item: AccessorialSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialSnapshotStore(
    var items: MutableMap<String, AccessorialSnapshot>,
    var order: MutableList<String>,
) : AccessorialSnapshotStore {
    override fun find(code: String): AccessorialSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialSnapshot) {
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

    override fun list(): MutableList<AccessorialSnapshot> {
        val out = mutableListOf<AccessorialSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialSnapshotStore(): AccessorialSnapshotStore {
    return MemoryAccessorialSnapshotStore(mutableMapOf<String, AccessorialSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialSnapshotStore(
    var inner: AccessorialSnapshotStore,
    var cache: TtlCache<AccessorialSnapshot>,
    var hits: Int,
    var misses: Int,
) : AccessorialSnapshotStore {
    override fun find(code: String): AccessorialSnapshot? {
        val hit = (cache.get(code) as AccessorialSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialSnapshot> = inner.list()
}

fun newCachedAccessorialSnapshotStore(inner: AccessorialSnapshotStore, ttl: Int, clock: Clock): AccessorialSnapshotStore {
    return CachedAccessorialSnapshotStore(
        inner = inner,
        cache = newTtlCache<AccessorialSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial snapshots. Empty fields match all. */
data class AccessorialSnapshotQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AccessorialSnapshot>): MutableList<AccessorialSnapshot> {
        val matched = filter(items) { candidate: AccessorialSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialSnapshots(store: AccessorialSnapshotStore, query: AccessorialSnapshotQuery): MutableList<AccessorialSnapshot> {
    return query.apply(store.list())
}
