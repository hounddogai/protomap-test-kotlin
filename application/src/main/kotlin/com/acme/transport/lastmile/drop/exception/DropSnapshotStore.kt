package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop snapshots by code. */
interface DropSnapshotStore {
    fun find(code: String): DropSnapshot?
    fun save(item: DropSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropSnapshotStore(
    var items: MutableMap<String, DropSnapshot>,
    var order: MutableList<String>,
) : DropSnapshotStore {
    override fun find(code: String): DropSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropSnapshot) {
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

    override fun list(): MutableList<DropSnapshot> {
        val out = mutableListOf<DropSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropSnapshotStore(): DropSnapshotStore {
    return MemoryDropSnapshotStore(mutableMapOf<String, DropSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropSnapshotStore(
    var inner: DropSnapshotStore,
    var cache: TtlCache<DropSnapshot>,
    var hits: Int,
    var misses: Int,
) : DropSnapshotStore {
    override fun find(code: String): DropSnapshot? {
        val hit = (cache.get(code) as DropSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropSnapshot> = inner.list()
}

fun newCachedDropSnapshotStore(inner: DropSnapshotStore, ttl: Int, clock: Clock): DropSnapshotStore {
    return CachedDropSnapshotStore(
        inner = inner,
        cache = newTtlCache<DropSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop snapshots. Empty fields match all. */
data class DropSnapshotQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropSnapshot>): MutableList<DropSnapshot> {
        val matched = filter(items) { candidate: DropSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropSnapshots(store: DropSnapshotStore, query: DropSnapshotQuery): MutableList<DropSnapshot> {
    return query.apply(store.list())
}
