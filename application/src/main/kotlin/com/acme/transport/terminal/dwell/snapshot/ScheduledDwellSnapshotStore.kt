package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dwell snapshots by code. */
interface ScheduledDwellSnapshotStore {
    fun find(code: String): ScheduledDwellSnapshot?
    fun save(item: ScheduledDwellSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDwellSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDwellSnapshotStore(
    var items: MutableMap<String, ScheduledDwellSnapshot>,
    var order: MutableList<String>,
) : ScheduledDwellSnapshotStore {
    override fun find(code: String): ScheduledDwellSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDwellSnapshot) {
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

    override fun list(): MutableList<ScheduledDwellSnapshot> {
        val out = mutableListOf<ScheduledDwellSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDwellSnapshotStore(): ScheduledDwellSnapshotStore {
    return MemoryScheduledDwellSnapshotStore(
        mutableMapOf<String, ScheduledDwellSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDwellSnapshotStore(
    var inner: ScheduledDwellSnapshotStore,
    var cache: TtlCache<ScheduledDwellSnapshot>,
    var hits: Int,
    var misses: Int,
) : ScheduledDwellSnapshotStore {
    override fun find(code: String): ScheduledDwellSnapshot? {
        val hit = (cache.get(code) as ScheduledDwellSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDwellSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDwellSnapshot> = inner.list()
}

fun newCachedScheduledDwellSnapshotStore(inner: ScheduledDwellSnapshotStore, ttl: Int, clock: Clock): ScheduledDwellSnapshotStore {
    return CachedScheduledDwellSnapshotStore(
        inner = inner,
        cache = newTtlCache<ScheduledDwellSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dwell snapshots. Empty fields match all. */
data class ScheduledDwellSnapshotQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDwellSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDwellSnapshot>): MutableList<ScheduledDwellSnapshot> {
        val matched = filter(items) { candidate: ScheduledDwellSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDwellSnapshots(store: ScheduledDwellSnapshotStore, query: ScheduledDwellSnapshotQuery): MutableList<ScheduledDwellSnapshot> {
    return query.apply(store.list())
}
