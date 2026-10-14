package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default backhaul snapshots by code. */
interface DefaultBackhaulSnapshotStore {
    fun find(code: String): DefaultBackhaulSnapshot?
    fun save(item: DefaultBackhaulSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultBackhaulSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultBackhaulSnapshotStore(
    var items: MutableMap<String, DefaultBackhaulSnapshot>,
    var order: MutableList<String>,
) : DefaultBackhaulSnapshotStore {
    override fun find(code: String): DefaultBackhaulSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultBackhaulSnapshot) {
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

    override fun list(): MutableList<DefaultBackhaulSnapshot> {
        val out = mutableListOf<DefaultBackhaulSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultBackhaulSnapshotStore(): DefaultBackhaulSnapshotStore {
    return MemoryDefaultBackhaulSnapshotStore(
        mutableMapOf<String, DefaultBackhaulSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultBackhaulSnapshotStore(
    var inner: DefaultBackhaulSnapshotStore,
    var cache: TtlCache<DefaultBackhaulSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultBackhaulSnapshotStore {
    override fun find(code: String): DefaultBackhaulSnapshot? {
        val hit = (cache.get(code) as DefaultBackhaulSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultBackhaulSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultBackhaulSnapshot> = inner.list()
}

fun newCachedDefaultBackhaulSnapshotStore(inner: DefaultBackhaulSnapshotStore, ttl: Int, clock: Clock): DefaultBackhaulSnapshotStore {
    return CachedDefaultBackhaulSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultBackhaulSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default backhaul snapshots. Empty fields match all. */
data class DefaultBackhaulSnapshotQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultBackhaulSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultBackhaulSnapshot>): MutableList<DefaultBackhaulSnapshot> {
        val matched = filter(items) { candidate: DefaultBackhaulSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultBackhaulSnapshots(store: DefaultBackhaulSnapshotStore, query: DefaultBackhaulSnapshotQuery): MutableList<DefaultBackhaulSnapshot> {
    return query.apply(store.list())
}
