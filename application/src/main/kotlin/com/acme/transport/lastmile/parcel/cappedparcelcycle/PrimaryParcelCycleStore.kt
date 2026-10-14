package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary parcel cycles by code. */
interface PrimaryParcelCycleStore {
    fun find(code: String): PrimaryParcelCycle?
    fun save(item: PrimaryParcelCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryParcelCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryParcelCycleStore(
    var items: MutableMap<String, PrimaryParcelCycle>,
    var order: MutableList<String>,
) : PrimaryParcelCycleStore {
    override fun find(code: String): PrimaryParcelCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryParcelCycle) {
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

    override fun list(): MutableList<PrimaryParcelCycle> {
        val out = mutableListOf<PrimaryParcelCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryParcelCycleStore(): PrimaryParcelCycleStore {
    return MemoryPrimaryParcelCycleStore(mutableMapOf<String, PrimaryParcelCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryParcelCycleStore(
    var inner: PrimaryParcelCycleStore,
    var cache: TtlCache<PrimaryParcelCycle>,
    var hits: Int,
    var misses: Int,
) : PrimaryParcelCycleStore {
    override fun find(code: String): PrimaryParcelCycle? {
        val hit = (cache.get(code) as PrimaryParcelCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryParcelCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryParcelCycle> = inner.list()
}

fun newCachedPrimaryParcelCycleStore(inner: PrimaryParcelCycleStore, ttl: Int, clock: Clock): PrimaryParcelCycleStore {
    return CachedPrimaryParcelCycleStore(
        inner = inner,
        cache = newTtlCache<PrimaryParcelCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary parcel cycles. Empty fields match all. */
data class PrimaryParcelCycleQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryParcelCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryParcelCycle>): MutableList<PrimaryParcelCycle> {
        val matched = filter(items) { candidate: PrimaryParcelCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryParcelCycles(store: PrimaryParcelCycleStore, query: PrimaryParcelCycleQuery): MutableList<PrimaryParcelCycle> {
    return query.apply(store.list())
}
