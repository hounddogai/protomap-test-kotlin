package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary backhaul rollups by code. */
interface PrimaryBackhaulRollupStore {
    fun find(code: String): PrimaryBackhaulRollup?
    fun save(item: PrimaryBackhaulRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBackhaulRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBackhaulRollupStore(
    var items: MutableMap<String, PrimaryBackhaulRollup>,
    var order: MutableList<String>,
) : PrimaryBackhaulRollupStore {
    override fun find(code: String): PrimaryBackhaulRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBackhaulRollup) {
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

    override fun list(): MutableList<PrimaryBackhaulRollup> {
        val out = mutableListOf<PrimaryBackhaulRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBackhaulRollupStore(): PrimaryBackhaulRollupStore {
    return MemoryPrimaryBackhaulRollupStore(
        mutableMapOf<String, PrimaryBackhaulRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBackhaulRollupStore(
    var inner: PrimaryBackhaulRollupStore,
    var cache: TtlCache<PrimaryBackhaulRollup>,
    var hits: Int,
    var misses: Int,
) : PrimaryBackhaulRollupStore {
    override fun find(code: String): PrimaryBackhaulRollup? {
        val hit = (cache.get(code) as PrimaryBackhaulRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBackhaulRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBackhaulRollup> = inner.list()
}

fun newCachedPrimaryBackhaulRollupStore(inner: PrimaryBackhaulRollupStore, ttl: Int, clock: Clock): PrimaryBackhaulRollupStore {
    return CachedPrimaryBackhaulRollupStore(
        inner = inner,
        cache = newTtlCache<PrimaryBackhaulRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary backhaul rollups. Empty fields match all. */
data class PrimaryBackhaulRollupQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBackhaulRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryBackhaulRollup>): MutableList<PrimaryBackhaulRollup> {
        val matched = filter(items) { candidate: PrimaryBackhaulRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBackhaulRollups(store: PrimaryBackhaulRollupStore, query: PrimaryBackhaulRollupQuery): MutableList<PrimaryBackhaulRollup> {
    return query.apply(store.list())
}
