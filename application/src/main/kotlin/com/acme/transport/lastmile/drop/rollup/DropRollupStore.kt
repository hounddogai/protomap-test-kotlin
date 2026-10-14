package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop rollups by code. */
interface DropRollupStore {
    fun find(code: String): DropRollup?
    fun save(item: DropRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropRollupStore(
    var items: MutableMap<String, DropRollup>,
    var order: MutableList<String>,
) : DropRollupStore {
    override fun find(code: String): DropRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropRollup) {
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

    override fun list(): MutableList<DropRollup> {
        val out = mutableListOf<DropRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropRollupStore(): DropRollupStore {
    return MemoryDropRollupStore(mutableMapOf<String, DropRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropRollupStore(
    var inner: DropRollupStore,
    var cache: TtlCache<DropRollup>,
    var hits: Int,
    var misses: Int,
) : DropRollupStore {
    override fun find(code: String): DropRollup? {
        val hit = (cache.get(code) as DropRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropRollup> = inner.list()
}

fun newCachedDropRollupStore(inner: DropRollupStore, ttl: Int, clock: Clock): DropRollupStore {
    return CachedDropRollupStore(
        inner = inner,
        cache = newTtlCache<DropRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop rollups. Empty fields match all. */
data class DropRollupQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropRollup>): MutableList<DropRollup> {
        val matched = filter(items) { candidate: DropRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropRollups(store: DropRollupStore, query: DropRollupQuery): MutableList<DropRollup> {
    return query.apply(store.list())
}
