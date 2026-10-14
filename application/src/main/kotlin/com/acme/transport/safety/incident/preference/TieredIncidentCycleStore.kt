package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered incident cycles by code. */
interface TieredIncidentCycleStore {
    fun find(code: String): TieredIncidentCycle?
    fun save(item: TieredIncidentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIncidentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIncidentCycleStore(
    var items: MutableMap<String, TieredIncidentCycle>,
    var order: MutableList<String>,
) : TieredIncidentCycleStore {
    override fun find(code: String): TieredIncidentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIncidentCycle) {
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

    override fun list(): MutableList<TieredIncidentCycle> {
        val out = mutableListOf<TieredIncidentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIncidentCycleStore(): TieredIncidentCycleStore {
    return MemoryTieredIncidentCycleStore(mutableMapOf<String, TieredIncidentCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIncidentCycleStore(
    var inner: TieredIncidentCycleStore,
    var cache: TtlCache<TieredIncidentCycle>,
    var hits: Int,
    var misses: Int,
) : TieredIncidentCycleStore {
    override fun find(code: String): TieredIncidentCycle? {
        val hit = (cache.get(code) as TieredIncidentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIncidentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIncidentCycle> = inner.list()
}

fun newCachedTieredIncidentCycleStore(inner: TieredIncidentCycleStore, ttl: Int, clock: Clock): TieredIncidentCycleStore {
    return CachedTieredIncidentCycleStore(
        inner = inner,
        cache = newTtlCache<TieredIncidentCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered incident cycles. Empty fields match all. */
data class TieredIncidentCycleQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIncidentCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredIncidentCycle>): MutableList<TieredIncidentCycle> {
        val matched = filter(items) { candidate: TieredIncidentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIncidentCycles(store: TieredIncidentCycleStore, query: TieredIncidentCycleQuery): MutableList<TieredIncidentCycle> {
    return query.apply(store.list())
}
