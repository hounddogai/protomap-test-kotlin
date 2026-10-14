package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default emission factor snapshots by code. */
interface DefaultEmissionFactorSnapshotStore {
    fun find(code: String): DefaultEmissionFactorSnapshot?
    fun save(item: DefaultEmissionFactorSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEmissionFactorSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEmissionFactorSnapshotStore(
    var items: MutableMap<String, DefaultEmissionFactorSnapshot>,
    var order: MutableList<String>,
) : DefaultEmissionFactorSnapshotStore {
    override fun find(code: String): DefaultEmissionFactorSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEmissionFactorSnapshot) {
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

    override fun list(): MutableList<DefaultEmissionFactorSnapshot> {
        val out = mutableListOf<DefaultEmissionFactorSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEmissionFactorSnapshotStore(): DefaultEmissionFactorSnapshotStore {
    return MemoryDefaultEmissionFactorSnapshotStore(
        mutableMapOf<String, DefaultEmissionFactorSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEmissionFactorSnapshotStore(
    var inner: DefaultEmissionFactorSnapshotStore,
    var cache: TtlCache<DefaultEmissionFactorSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultEmissionFactorSnapshotStore {
    override fun find(code: String): DefaultEmissionFactorSnapshot? {
        val hit = (cache.get(code) as DefaultEmissionFactorSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEmissionFactorSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEmissionFactorSnapshot> = inner.list()
}

fun newCachedDefaultEmissionFactorSnapshotStore(inner: DefaultEmissionFactorSnapshotStore, ttl: Int, clock: Clock): DefaultEmissionFactorSnapshotStore {
    return CachedDefaultEmissionFactorSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultEmissionFactorSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default emission factor snapshots. Empty fields match all. */
data class DefaultEmissionFactorSnapshotQuery(
    var unit: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEmissionFactorSnapshot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEmissionFactorSnapshot>): MutableList<DefaultEmissionFactorSnapshot> {
        val matched = filter(items) { candidate: DefaultEmissionFactorSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEmissionFactorSnapshots(store: DefaultEmissionFactorSnapshotStore, query: DefaultEmissionFactorSnapshotQuery): MutableList<DefaultEmissionFactorSnapshot> {
    return query.apply(store.list())
}
