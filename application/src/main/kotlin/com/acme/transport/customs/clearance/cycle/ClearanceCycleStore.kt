package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance cycles by code. */
interface ClearanceCycleStore {
    fun find(code: String): ClearanceCycle?
    fun save(item: ClearanceCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceCycleStore(
    var items: MutableMap<String, ClearanceCycle>,
    var order: MutableList<String>,
) : ClearanceCycleStore {
    override fun find(code: String): ClearanceCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceCycle) {
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

    override fun list(): MutableList<ClearanceCycle> {
        val out = mutableListOf<ClearanceCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceCycleStore(): ClearanceCycleStore {
    return MemoryClearanceCycleStore(mutableMapOf<String, ClearanceCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceCycleStore(
    var inner: ClearanceCycleStore,
    var cache: TtlCache<ClearanceCycle>,
    var hits: Int,
    var misses: Int,
) : ClearanceCycleStore {
    override fun find(code: String): ClearanceCycle? {
        val hit = (cache.get(code) as ClearanceCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceCycle> = inner.list()
}

fun newCachedClearanceCycleStore(inner: ClearanceCycleStore, ttl: Int, clock: Clock): ClearanceCycleStore {
    return CachedClearanceCycleStore(
        inner = inner,
        cache = newTtlCache<ClearanceCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance cycles. Empty fields match all. */
data class ClearanceCycleQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceCycle>): MutableList<ClearanceCycle> {
        val matched = filter(items) { candidate: ClearanceCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceCycles(store: ClearanceCycleStore, query: ClearanceCycleQuery): MutableList<ClearanceCycle> {
    return query.apply(store.list())
}
