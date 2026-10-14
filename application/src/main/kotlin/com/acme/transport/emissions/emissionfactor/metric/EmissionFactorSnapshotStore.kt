package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor snapshots by code. */
interface EmissionFactorSnapshotStore {
    fun find(code: String): EmissionFactorSnapshot?
    fun save(item: EmissionFactorSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorSnapshotStore(
    var items: MutableMap<String, EmissionFactorSnapshot>,
    var order: MutableList<String>,
) : EmissionFactorSnapshotStore {
    override fun find(code: String): EmissionFactorSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorSnapshot) {
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

    override fun list(): MutableList<EmissionFactorSnapshot> {
        val out = mutableListOf<EmissionFactorSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorSnapshotStore(): EmissionFactorSnapshotStore {
    return MemoryEmissionFactorSnapshotStore(
        mutableMapOf<String, EmissionFactorSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorSnapshotStore(
    var inner: EmissionFactorSnapshotStore,
    var cache: TtlCache<EmissionFactorSnapshot>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorSnapshotStore {
    override fun find(code: String): EmissionFactorSnapshot? {
        val hit = (cache.get(code) as EmissionFactorSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorSnapshot> = inner.list()
}

fun newCachedEmissionFactorSnapshotStore(inner: EmissionFactorSnapshotStore, ttl: Int, clock: Clock): EmissionFactorSnapshotStore {
    return CachedEmissionFactorSnapshotStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor snapshots. Empty fields match all. */
data class EmissionFactorSnapshotQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorSnapshot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<EmissionFactorSnapshot>): MutableList<EmissionFactorSnapshot> {
        val matched = filter(items) { candidate: EmissionFactorSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorSnapshots(store: EmissionFactorSnapshotStore, query: EmissionFactorSnapshotQuery): MutableList<EmissionFactorSnapshot> {
    return query.apply(store.list())
}
