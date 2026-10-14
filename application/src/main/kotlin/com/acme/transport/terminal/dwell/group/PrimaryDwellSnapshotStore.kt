package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary dwell snapshots by code. */
interface PrimaryDwellSnapshotStore {
    fun find(code: String): PrimaryDwellSnapshot?
    fun save(item: PrimaryDwellSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDwellSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDwellSnapshotStore(
    var items: MutableMap<String, PrimaryDwellSnapshot>,
    var order: MutableList<String>,
) : PrimaryDwellSnapshotStore {
    override fun find(code: String): PrimaryDwellSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDwellSnapshot) {
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

    override fun list(): MutableList<PrimaryDwellSnapshot> {
        val out = mutableListOf<PrimaryDwellSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDwellSnapshotStore(): PrimaryDwellSnapshotStore {
    return MemoryPrimaryDwellSnapshotStore(
        mutableMapOf<String, PrimaryDwellSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDwellSnapshotStore(
    var inner: PrimaryDwellSnapshotStore,
    var cache: TtlCache<PrimaryDwellSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryDwellSnapshotStore {
    override fun find(code: String): PrimaryDwellSnapshot? {
        val hit = (cache.get(code) as PrimaryDwellSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDwellSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDwellSnapshot> = inner.list()
}

fun newCachedPrimaryDwellSnapshotStore(inner: PrimaryDwellSnapshotStore, ttl: Int, clock: Clock): PrimaryDwellSnapshotStore {
    return CachedPrimaryDwellSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryDwellSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary dwell snapshots. Empty fields match all. */
data class PrimaryDwellSnapshotQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDwellSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDwellSnapshot>): MutableList<PrimaryDwellSnapshot> {
        val matched = filter(items) { candidate: PrimaryDwellSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDwellSnapshots(store: PrimaryDwellSnapshotStore, query: PrimaryDwellSnapshotQuery): MutableList<PrimaryDwellSnapshot> {
    return query.apply(store.list())
}
