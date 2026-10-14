package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire cycles by code. */
interface TireCycleStore {
    fun find(code: String): TireCycle?
    fun save(item: TireCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireCycleStore(
    var items: MutableMap<String, TireCycle>,
    var order: MutableList<String>,
) : TireCycleStore {
    override fun find(code: String): TireCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireCycle) {
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

    override fun list(): MutableList<TireCycle> {
        val out = mutableListOf<TireCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireCycleStore(): TireCycleStore {
    return MemoryTireCycleStore(mutableMapOf<String, TireCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireCycleStore(
    var inner: TireCycleStore,
    var cache: TtlCache<TireCycle>,
    var hits: Int,
    var misses: Int,
) : TireCycleStore {
    override fun find(code: String): TireCycle? {
        val hit = (cache.get(code) as TireCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireCycle> = inner.list()
}

fun newCachedTireCycleStore(inner: TireCycleStore, ttl: Int, clock: Clock): TireCycleStore {
    return CachedTireCycleStore(
        inner = inner,
        cache = newTtlCache<TireCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire cycles. Empty fields match all. */
data class TireCycleQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireCycle>): MutableList<TireCycle> {
        val matched = filter(items) { candidate: TireCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireCycles(store: TireCycleStore, query: TireCycleQuery): MutableList<TireCycle> {
    return query.apply(store.list())
}
