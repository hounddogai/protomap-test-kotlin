package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold rollups by code. */
interface HoldRollupStore {
    fun find(code: String): HoldRollup?
    fun save(item: HoldRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldRollupStore(
    var items: MutableMap<String, HoldRollup>,
    var order: MutableList<String>,
) : HoldRollupStore {
    override fun find(code: String): HoldRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldRollup) {
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

    override fun list(): MutableList<HoldRollup> {
        val out = mutableListOf<HoldRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldRollupStore(): HoldRollupStore {
    return MemoryHoldRollupStore(mutableMapOf<String, HoldRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldRollupStore(
    var inner: HoldRollupStore,
    var cache: TtlCache<HoldRollup>,
    var hits: Int,
    var misses: Int,
) : HoldRollupStore {
    override fun find(code: String): HoldRollup? {
        val hit = (cache.get(code) as HoldRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldRollup> = inner.list()
}

fun newCachedHoldRollupStore(inner: HoldRollupStore, ttl: Int, clock: Clock): HoldRollupStore {
    return CachedHoldRollupStore(
        inner = inner,
        cache = newTtlCache<HoldRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold rollups. Empty fields match all. */
data class HoldRollupQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldRollup>): MutableList<HoldRollup> {
        val matched = filter(items) { candidate: HoldRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldRollups(store: HoldRollupStore, query: HoldRollupQuery): MutableList<HoldRollup> {
    return query.apply(store.list())
}
