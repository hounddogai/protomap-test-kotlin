package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient node snapshots by code. */
interface LenientNodeSnapshotStore {
    fun find(code: String): LenientNodeSnapshot?
    fun save(item: LenientNodeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNodeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNodeSnapshotStore(
    var items: MutableMap<String, LenientNodeSnapshot>,
    var order: MutableList<String>,
) : LenientNodeSnapshotStore {
    override fun find(code: String): LenientNodeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNodeSnapshot) {
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

    override fun list(): MutableList<LenientNodeSnapshot> {
        val out = mutableListOf<LenientNodeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNodeSnapshotStore(): LenientNodeSnapshotStore {
    return MemoryLenientNodeSnapshotStore(mutableMapOf<String, LenientNodeSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNodeSnapshotStore(
    var inner: LenientNodeSnapshotStore,
    var cache: TtlCache<LenientNodeSnapshot>,
    var hits: Int,
    var misses: Int,
) : LenientNodeSnapshotStore {
    override fun find(code: String): LenientNodeSnapshot? {
        val hit = (cache.get(code) as LenientNodeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNodeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNodeSnapshot> = inner.list()
}

fun newCachedLenientNodeSnapshotStore(inner: LenientNodeSnapshotStore, ttl: Int, clock: Clock): LenientNodeSnapshotStore {
    return CachedLenientNodeSnapshotStore(
        inner = inner,
        cache = newTtlCache<LenientNodeSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient node snapshots. Empty fields match all. */
data class LenientNodeSnapshotQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNodeSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LenientNodeSnapshot>): MutableList<LenientNodeSnapshot> {
        val matched = filter(items) { candidate: LenientNodeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNodeSnapshots(store: LenientNodeSnapshotStore, query: LenientNodeSnapshotQuery): MutableList<LenientNodeSnapshot> {
    return query.apply(store.list())
}
