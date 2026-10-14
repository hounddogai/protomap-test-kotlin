package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff snapshots by code. */
interface TariffSnapshotStore {
    fun find(code: String): TariffSnapshot?
    fun save(item: TariffSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffSnapshotStore(
    var items: MutableMap<String, TariffSnapshot>,
    var order: MutableList<String>,
) : TariffSnapshotStore {
    override fun find(code: String): TariffSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffSnapshot) {
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

    override fun list(): MutableList<TariffSnapshot> {
        val out = mutableListOf<TariffSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffSnapshotStore(): TariffSnapshotStore {
    return MemoryTariffSnapshotStore(mutableMapOf<String, TariffSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffSnapshotStore(
    var inner: TariffSnapshotStore,
    var cache: TtlCache<TariffSnapshot>,
    var hits: Int,
    var misses: Int,
) : TariffSnapshotStore {
    override fun find(code: String): TariffSnapshot? {
        val hit = (cache.get(code) as TariffSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffSnapshot> = inner.list()
}

fun newCachedTariffSnapshotStore(inner: TariffSnapshotStore, ttl: Int, clock: Clock): TariffSnapshotStore {
    return CachedTariffSnapshotStore(
        inner = inner,
        cache = newTtlCache<TariffSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff snapshots. Empty fields match all. */
data class TariffSnapshotQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffSnapshot>): MutableList<TariffSnapshot> {
        val matched = filter(items) { candidate: TariffSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffSnapshots(store: TariffSnapshotStore, query: TariffSnapshotQuery): MutableList<TariffSnapshot> {
    return query.apply(store.list())
}
