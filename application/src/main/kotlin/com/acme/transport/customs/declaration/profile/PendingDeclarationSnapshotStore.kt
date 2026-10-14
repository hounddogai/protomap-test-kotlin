package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending declaration snapshots by code. */
interface PendingDeclarationSnapshotStore {
    fun find(code: String): PendingDeclarationSnapshot?
    fun save(item: PendingDeclarationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDeclarationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDeclarationSnapshotStore(
    var items: MutableMap<String, PendingDeclarationSnapshot>,
    var order: MutableList<String>,
) : PendingDeclarationSnapshotStore {
    override fun find(code: String): PendingDeclarationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDeclarationSnapshot) {
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

    override fun list(): MutableList<PendingDeclarationSnapshot> {
        val out = mutableListOf<PendingDeclarationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDeclarationSnapshotStore(): PendingDeclarationSnapshotStore {
    return MemoryPendingDeclarationSnapshotStore(
        mutableMapOf<String, PendingDeclarationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDeclarationSnapshotStore(
    var inner: PendingDeclarationSnapshotStore,
    var cache: TtlCache<PendingDeclarationSnapshot>,
    var hits: Int,
    var misses: Int,
) : PendingDeclarationSnapshotStore {
    override fun find(code: String): PendingDeclarationSnapshot? {
        val hit = (cache.get(code) as PendingDeclarationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDeclarationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDeclarationSnapshot> = inner.list()
}

fun newCachedPendingDeclarationSnapshotStore(inner: PendingDeclarationSnapshotStore, ttl: Int, clock: Clock): PendingDeclarationSnapshotStore {
    return CachedPendingDeclarationSnapshotStore(
        inner = inner,
        cache = newTtlCache<PendingDeclarationSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending declaration snapshots. Empty fields match all. */
data class PendingDeclarationSnapshotQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDeclarationSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDeclarationSnapshot>): MutableList<PendingDeclarationSnapshot> {
        val matched = filter(items) { candidate: PendingDeclarationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDeclarationSnapshots(store: PendingDeclarationSnapshotStore, query: PendingDeclarationSnapshotQuery): MutableList<PendingDeclarationSnapshot> {
    return query.apply(store.list())
}
