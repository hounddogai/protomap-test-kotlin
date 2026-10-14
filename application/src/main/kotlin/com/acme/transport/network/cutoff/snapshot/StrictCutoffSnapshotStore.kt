package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict cutoff snapshots by code. */
interface StrictCutoffSnapshotStore {
    fun find(code: String): StrictCutoffSnapshot?
    fun save(item: StrictCutoffSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCutoffSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCutoffSnapshotStore(
    var items: MutableMap<String, StrictCutoffSnapshot>,
    var order: MutableList<String>,
) : StrictCutoffSnapshotStore {
    override fun find(code: String): StrictCutoffSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCutoffSnapshot) {
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

    override fun list(): MutableList<StrictCutoffSnapshot> {
        val out = mutableListOf<StrictCutoffSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCutoffSnapshotStore(): StrictCutoffSnapshotStore {
    return MemoryStrictCutoffSnapshotStore(
        mutableMapOf<String, StrictCutoffSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCutoffSnapshotStore(
    var inner: StrictCutoffSnapshotStore,
    var cache: TtlCache<StrictCutoffSnapshot>,
    var hits: Int,
    var misses: Int,
) : StrictCutoffSnapshotStore {
    override fun find(code: String): StrictCutoffSnapshot? {
        val hit = (cache.get(code) as StrictCutoffSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCutoffSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCutoffSnapshot> = inner.list()
}

fun newCachedStrictCutoffSnapshotStore(inner: StrictCutoffSnapshotStore, ttl: Int, clock: Clock): StrictCutoffSnapshotStore {
    return CachedStrictCutoffSnapshotStore(
        inner = inner,
        cache = newTtlCache<StrictCutoffSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict cutoff snapshots. Empty fields match all. */
data class StrictCutoffSnapshotQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCutoffSnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<StrictCutoffSnapshot>): MutableList<StrictCutoffSnapshot> {
        val matched = filter(items) { candidate: StrictCutoffSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCutoffSnapshots(store: StrictCutoffSnapshotStore, query: StrictCutoffSnapshotQuery): MutableList<StrictCutoffSnapshot> {
    return query.apply(store.list())
}
