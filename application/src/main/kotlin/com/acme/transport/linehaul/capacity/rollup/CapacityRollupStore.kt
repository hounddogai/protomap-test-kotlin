package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity rollups by code. */
interface CapacityRollupStore {
    fun find(code: String): CapacityRollup?
    fun save(item: CapacityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityRollupStore(
    var items: MutableMap<String, CapacityRollup>,
    var order: MutableList<String>,
) : CapacityRollupStore {
    override fun find(code: String): CapacityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityRollup) {
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

    override fun list(): MutableList<CapacityRollup> {
        val out = mutableListOf<CapacityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityRollupStore(): CapacityRollupStore {
    return MemoryCapacityRollupStore(mutableMapOf<String, CapacityRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityRollupStore(
    var inner: CapacityRollupStore,
    var cache: TtlCache<CapacityRollup>,
    var hits: Int,
    var misses: Int,
) : CapacityRollupStore {
    override fun find(code: String): CapacityRollup? {
        val hit = (cache.get(code) as CapacityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityRollup> = inner.list()
}

fun newCachedCapacityRollupStore(inner: CapacityRollupStore, ttl: Int, clock: Clock): CapacityRollupStore {
    return CachedCapacityRollupStore(
        inner = inner,
        cache = newTtlCache<CapacityRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity rollups. Empty fields match all. */
data class CapacityRollupQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityRollup>): MutableList<CapacityRollup> {
        val matched = filter(items) { candidate: CapacityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityRollups(store: CapacityRollupStore, query: CapacityRollupQuery): MutableList<CapacityRollup> {
    return query.apply(store.list())
}
