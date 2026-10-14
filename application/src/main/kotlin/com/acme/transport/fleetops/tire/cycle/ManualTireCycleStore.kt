package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tire cycles by code. */
interface ManualTireCycleStore {
    fun find(code: String): ManualTireCycle?
    fun save(item: ManualTireCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTireCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTireCycleStore(
    var items: MutableMap<String, ManualTireCycle>,
    var order: MutableList<String>,
) : ManualTireCycleStore {
    override fun find(code: String): ManualTireCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTireCycle) {
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

    override fun list(): MutableList<ManualTireCycle> {
        val out = mutableListOf<ManualTireCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTireCycleStore(): ManualTireCycleStore {
    return MemoryManualTireCycleStore(mutableMapOf<String, ManualTireCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTireCycleStore(
    var inner: ManualTireCycleStore,
    var cache: TtlCache<ManualTireCycle>,
    var hits: Int,
    var misses: Int,
) : ManualTireCycleStore {
    override fun find(code: String): ManualTireCycle? {
        val hit = (cache.get(code) as ManualTireCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTireCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTireCycle> = inner.list()
}

fun newCachedManualTireCycleStore(inner: ManualTireCycleStore, ttl: Int, clock: Clock): ManualTireCycleStore {
    return CachedManualTireCycleStore(
        inner = inner,
        cache = newTtlCache<ManualTireCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tire cycles. Empty fields match all. */
data class ManualTireCycleQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTireCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTireCycle>): MutableList<ManualTireCycle> {
        val matched = filter(items) { candidate: ManualTireCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTireCycles(store: ManualTireCycleStore, query: ManualTireCycleQuery): MutableList<ManualTireCycle> {
    return query.apply(store.list())
}
