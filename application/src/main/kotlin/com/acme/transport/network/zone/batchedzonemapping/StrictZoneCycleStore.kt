package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict zone cycles by code. */
interface StrictZoneCycleStore {
    fun find(code: String): StrictZoneCycle?
    fun save(item: StrictZoneCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictZoneCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictZoneCycleStore(
    var items: MutableMap<String, StrictZoneCycle>,
    var order: MutableList<String>,
) : StrictZoneCycleStore {
    override fun find(code: String): StrictZoneCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictZoneCycle) {
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

    override fun list(): MutableList<StrictZoneCycle> {
        val out = mutableListOf<StrictZoneCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictZoneCycleStore(): StrictZoneCycleStore {
    return MemoryStrictZoneCycleStore(mutableMapOf<String, StrictZoneCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictZoneCycleStore(
    var inner: StrictZoneCycleStore,
    var cache: TtlCache<StrictZoneCycle>,
    var hits: Int,
    var misses: Int,
) : StrictZoneCycleStore {
    override fun find(code: String): StrictZoneCycle? {
        val hit = (cache.get(code) as StrictZoneCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictZoneCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictZoneCycle> = inner.list()
}

fun newCachedStrictZoneCycleStore(inner: StrictZoneCycleStore, ttl: Int, clock: Clock): StrictZoneCycleStore {
    return CachedStrictZoneCycleStore(
        inner = inner,
        cache = newTtlCache<StrictZoneCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict zone cycles. Empty fields match all. */
data class StrictZoneCycleQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictZoneCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictZoneCycle>): MutableList<StrictZoneCycle> {
        val matched = filter(items) { candidate: StrictZoneCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictZoneCycles(store: StrictZoneCycleStore, query: StrictZoneCycleQuery): MutableList<StrictZoneCycle> {
    return query.apply(store.list())
}
