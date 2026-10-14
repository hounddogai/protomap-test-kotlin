package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict parcel cycles by code. */
interface StrictParcelCycleStore {
    fun find(code: String): StrictParcelCycle?
    fun save(item: StrictParcelCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictParcelCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictParcelCycleStore(
    var items: MutableMap<String, StrictParcelCycle>,
    var order: MutableList<String>,
) : StrictParcelCycleStore {
    override fun find(code: String): StrictParcelCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictParcelCycle) {
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

    override fun list(): MutableList<StrictParcelCycle> {
        val out = mutableListOf<StrictParcelCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictParcelCycleStore(): StrictParcelCycleStore {
    return MemoryStrictParcelCycleStore(mutableMapOf<String, StrictParcelCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictParcelCycleStore(
    var inner: StrictParcelCycleStore,
    var cache: TtlCache<StrictParcelCycle>,
    var hits: Int,
    var misses: Int,
) : StrictParcelCycleStore {
    override fun find(code: String): StrictParcelCycle? {
        val hit = (cache.get(code) as StrictParcelCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictParcelCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictParcelCycle> = inner.list()
}

fun newCachedStrictParcelCycleStore(inner: StrictParcelCycleStore, ttl: Int, clock: Clock): StrictParcelCycleStore {
    return CachedStrictParcelCycleStore(
        inner = inner,
        cache = newTtlCache<StrictParcelCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict parcel cycles. Empty fields match all. */
data class StrictParcelCycleQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictParcelCycle): Boolean {
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

    fun apply(items: MutableList<StrictParcelCycle>): MutableList<StrictParcelCycle> {
        val matched = filter(items) { candidate: StrictParcelCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictParcelCycles(store: StrictParcelCycleStore, query: StrictParcelCycleQuery): MutableList<StrictParcelCycle> {
    return query.apply(store.list())
}
