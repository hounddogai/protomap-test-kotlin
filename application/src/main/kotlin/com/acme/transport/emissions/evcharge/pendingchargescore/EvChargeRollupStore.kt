package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge rollups by code. */
interface EvChargeRollupStore {
    fun find(code: String): EvChargeRollup?
    fun save(item: EvChargeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeRollupStore(
    var items: MutableMap<String, EvChargeRollup>,
    var order: MutableList<String>,
) : EvChargeRollupStore {
    override fun find(code: String): EvChargeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeRollup) {
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

    override fun list(): MutableList<EvChargeRollup> {
        val out = mutableListOf<EvChargeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeRollupStore(): EvChargeRollupStore {
    return MemoryEvChargeRollupStore(mutableMapOf<String, EvChargeRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeRollupStore(
    var inner: EvChargeRollupStore,
    var cache: TtlCache<EvChargeRollup>,
    var hits: Int,
    var misses: Int,
) : EvChargeRollupStore {
    override fun find(code: String): EvChargeRollup? {
        val hit = (cache.get(code) as EvChargeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeRollup> = inner.list()
}

fun newCachedEvChargeRollupStore(inner: EvChargeRollupStore, ttl: Int, clock: Clock): EvChargeRollupStore {
    return CachedEvChargeRollupStore(
        inner = inner,
        cache = newTtlCache<EvChargeRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge rollups. Empty fields match all. */
data class EvChargeRollupQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeRollup>): MutableList<EvChargeRollup> {
        val matched = filter(items) { candidate: EvChargeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeRollups(store: EvChargeRollupStore, query: EvChargeRollupQuery): MutableList<EvChargeRollup> {
    return query.apply(store.list())
}
