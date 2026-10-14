package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge cycles by code. */
interface EvChargeCycleStore {
    fun find(code: String): EvChargeCycle?
    fun save(item: EvChargeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeCycleStore(
    var items: MutableMap<String, EvChargeCycle>,
    var order: MutableList<String>,
) : EvChargeCycleStore {
    override fun find(code: String): EvChargeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeCycle) {
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

    override fun list(): MutableList<EvChargeCycle> {
        val out = mutableListOf<EvChargeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeCycleStore(): EvChargeCycleStore {
    return MemoryEvChargeCycleStore(mutableMapOf<String, EvChargeCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeCycleStore(
    var inner: EvChargeCycleStore,
    var cache: TtlCache<EvChargeCycle>,
    var hits: Int,
    var misses: Int,
) : EvChargeCycleStore {
    override fun find(code: String): EvChargeCycle? {
        val hit = (cache.get(code) as EvChargeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeCycle> = inner.list()
}

fun newCachedEvChargeCycleStore(inner: EvChargeCycleStore, ttl: Int, clock: Clock): EvChargeCycleStore {
    return CachedEvChargeCycleStore(
        inner = inner,
        cache = newTtlCache<EvChargeCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge cycles. Empty fields match all. */
data class EvChargeCycleQuery(
    var unit: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeCycle>): MutableList<EvChargeCycle> {
        val matched = filter(items) { candidate: EvChargeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeCycles(store: EvChargeCycleStore, query: EvChargeCycleQuery): MutableList<EvChargeCycle> {
    return query.apply(store.list())
}
