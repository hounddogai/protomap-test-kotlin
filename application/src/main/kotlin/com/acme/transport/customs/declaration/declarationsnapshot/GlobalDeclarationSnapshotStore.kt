package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global declaration snapshots by code. */
interface GlobalDeclarationSnapshotStore {
    fun find(code: String): GlobalDeclarationSnapshot?
    fun save(item: GlobalDeclarationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDeclarationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDeclarationSnapshotStore(
    var items: MutableMap<String, GlobalDeclarationSnapshot>,
    var order: MutableList<String>,
) : GlobalDeclarationSnapshotStore {
    override fun find(code: String): GlobalDeclarationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDeclarationSnapshot) {
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

    override fun list(): MutableList<GlobalDeclarationSnapshot> {
        val out = mutableListOf<GlobalDeclarationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDeclarationSnapshotStore(): GlobalDeclarationSnapshotStore {
    return MemoryGlobalDeclarationSnapshotStore(
        mutableMapOf<String, GlobalDeclarationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDeclarationSnapshotStore(
    var inner: GlobalDeclarationSnapshotStore,
    var cache: TtlCache<GlobalDeclarationSnapshot>,
    var hits: Int,
    var misses: Int,
) : GlobalDeclarationSnapshotStore {
    override fun find(code: String): GlobalDeclarationSnapshot? {
        val hit = (cache.get(code) as GlobalDeclarationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDeclarationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDeclarationSnapshot> = inner.list()
}

fun newCachedGlobalDeclarationSnapshotStore(inner: GlobalDeclarationSnapshotStore, ttl: Int, clock: Clock): GlobalDeclarationSnapshotStore {
    return CachedGlobalDeclarationSnapshotStore(
        inner = inner,
        cache = newTtlCache<GlobalDeclarationSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global declaration snapshots. Empty fields match all. */
data class GlobalDeclarationSnapshotQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDeclarationSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<GlobalDeclarationSnapshot>): MutableList<GlobalDeclarationSnapshot> {
        val matched = filter(items) { candidate: GlobalDeclarationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDeclarationSnapshots(store: GlobalDeclarationSnapshotStore, query: GlobalDeclarationSnapshotQuery): MutableList<GlobalDeclarationSnapshot> {
    return query.apply(store.list())
}
