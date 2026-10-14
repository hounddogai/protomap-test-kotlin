package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient drop snapshots by code. */
interface LenientDropSnapshotStore {
    fun find(code: String): LenientDropSnapshot?
    fun save(item: LenientDropSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDropSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDropSnapshotStore(
    var items: MutableMap<String, LenientDropSnapshot>,
    var order: MutableList<String>,
) : LenientDropSnapshotStore {
    override fun find(code: String): LenientDropSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDropSnapshot) {
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

    override fun list(): MutableList<LenientDropSnapshot> {
        val out = mutableListOf<LenientDropSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDropSnapshotStore(): LenientDropSnapshotStore {
    return MemoryLenientDropSnapshotStore(mutableMapOf<String, LenientDropSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDropSnapshotStore(
    var inner: LenientDropSnapshotStore,
    var cache: TtlCache<LenientDropSnapshot>,
    var hits: Int,
    var misses: Int,
) : LenientDropSnapshotStore {
    override fun find(code: String): LenientDropSnapshot? {
        val hit = (cache.get(code) as LenientDropSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDropSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDropSnapshot> = inner.list()
}

fun newCachedLenientDropSnapshotStore(inner: LenientDropSnapshotStore, ttl: Int, clock: Clock): LenientDropSnapshotStore {
    return CachedLenientDropSnapshotStore(
        inner = inner,
        cache = newTtlCache<LenientDropSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient drop snapshots. Empty fields match all. */
data class LenientDropSnapshotQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDropSnapshot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDropSnapshot>): MutableList<LenientDropSnapshot> {
        val matched = filter(items) { candidate: LenientDropSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDropSnapshots(store: LenientDropSnapshotStore, query: LenientDropSnapshotQuery): MutableList<LenientDropSnapshot> {
    return query.apply(store.list())
}
