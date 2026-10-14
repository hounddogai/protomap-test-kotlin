package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy sortation snapshots by code. */
interface LegacySortationSnapshotStore {
    fun find(code: String): LegacySortationSnapshot?
    fun save(item: LegacySortationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySortationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySortationSnapshotStore(
    var items: MutableMap<String, LegacySortationSnapshot>,
    var order: MutableList<String>,
) : LegacySortationSnapshotStore {
    override fun find(code: String): LegacySortationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySortationSnapshot) {
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

    override fun list(): MutableList<LegacySortationSnapshot> {
        val out = mutableListOf<LegacySortationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySortationSnapshotStore(): LegacySortationSnapshotStore {
    return MemoryLegacySortationSnapshotStore(
        mutableMapOf<String, LegacySortationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySortationSnapshotStore(
    var inner: LegacySortationSnapshotStore,
    var cache: TtlCache<LegacySortationSnapshot>,
    var hits: Int,
    var misses: Int,
) : LegacySortationSnapshotStore {
    override fun find(code: String): LegacySortationSnapshot? {
        val hit = (cache.get(code) as LegacySortationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySortationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySortationSnapshot> = inner.list()
}

fun newCachedLegacySortationSnapshotStore(inner: LegacySortationSnapshotStore, ttl: Int, clock: Clock): LegacySortationSnapshotStore {
    return CachedLegacySortationSnapshotStore(
        inner = inner,
        cache = newTtlCache<LegacySortationSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy sortation snapshots. Empty fields match all. */
data class LegacySortationSnapshotQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySortationSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacySortationSnapshot>): MutableList<LegacySortationSnapshot> {
        val matched = filter(items) { candidate: LegacySortationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySortationSnapshots(store: LegacySortationSnapshotStore, query: LegacySortationSnapshotQuery): MutableList<LegacySortationSnapshot> {
    return query.apply(store.list())
}
