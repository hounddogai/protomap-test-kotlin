package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul snapshots by code. */
interface BackhaulSnapshotStore {
    fun find(code: String): BackhaulSnapshot?
    fun save(item: BackhaulSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulSnapshotStore(
    var items: MutableMap<String, BackhaulSnapshot>,
    var order: MutableList<String>,
) : BackhaulSnapshotStore {
    override fun find(code: String): BackhaulSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulSnapshot) {
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

    override fun list(): MutableList<BackhaulSnapshot> {
        val out = mutableListOf<BackhaulSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulSnapshotStore(): BackhaulSnapshotStore {
    return MemoryBackhaulSnapshotStore(mutableMapOf<String, BackhaulSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulSnapshotStore(
    var inner: BackhaulSnapshotStore,
    var cache: TtlCache<BackhaulSnapshot>,
    var hits: Int,
    var misses: Int,
) : BackhaulSnapshotStore {
    override fun find(code: String): BackhaulSnapshot? {
        val hit = (cache.get(code) as BackhaulSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulSnapshot> = inner.list()
}

fun newCachedBackhaulSnapshotStore(inner: BackhaulSnapshotStore, ttl: Int, clock: Clock): BackhaulSnapshotStore {
    return CachedBackhaulSnapshotStore(
        inner = inner,
        cache = newTtlCache<BackhaulSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul snapshots. Empty fields match all. */
data class BackhaulSnapshotQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulSnapshot>): MutableList<BackhaulSnapshot> {
        val matched = filter(items) { candidate: BackhaulSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulSnapshots(store: BackhaulSnapshotStore, query: BackhaulSnapshotQuery): MutableList<BackhaulSnapshot> {
    return query.apply(store.list())
}
