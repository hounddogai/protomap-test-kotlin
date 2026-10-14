package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default cross dock snapshots by code. */
interface DefaultCrossDockSnapshotStore {
    fun find(code: String): DefaultCrossDockSnapshot?
    fun save(item: DefaultCrossDockSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCrossDockSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCrossDockSnapshotStore(
    var items: MutableMap<String, DefaultCrossDockSnapshot>,
    var order: MutableList<String>,
) : DefaultCrossDockSnapshotStore {
    override fun find(code: String): DefaultCrossDockSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCrossDockSnapshot) {
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

    override fun list(): MutableList<DefaultCrossDockSnapshot> {
        val out = mutableListOf<DefaultCrossDockSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCrossDockSnapshotStore(): DefaultCrossDockSnapshotStore {
    return MemoryDefaultCrossDockSnapshotStore(
        mutableMapOf<String, DefaultCrossDockSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCrossDockSnapshotStore(
    var inner: DefaultCrossDockSnapshotStore,
    var cache: TtlCache<DefaultCrossDockSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultCrossDockSnapshotStore {
    override fun find(code: String): DefaultCrossDockSnapshot? {
        val hit = (cache.get(code) as DefaultCrossDockSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCrossDockSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCrossDockSnapshot> = inner.list()
}

fun newCachedDefaultCrossDockSnapshotStore(inner: DefaultCrossDockSnapshotStore, ttl: Int, clock: Clock): DefaultCrossDockSnapshotStore {
    return CachedDefaultCrossDockSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultCrossDockSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cross dock snapshots. Empty fields match all. */
data class DefaultCrossDockSnapshotQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCrossDockSnapshot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCrossDockSnapshot>): MutableList<DefaultCrossDockSnapshot> {
        val matched = filter(items) { candidate: DefaultCrossDockSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCrossDockSnapshots(store: DefaultCrossDockSnapshotStore, query: DefaultCrossDockSnapshotQuery): MutableList<DefaultCrossDockSnapshot> {
    return query.apply(store.list())
}
