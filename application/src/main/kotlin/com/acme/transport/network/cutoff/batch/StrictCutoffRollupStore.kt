package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict cutoff rollups by code. */
interface StrictCutoffRollupStore {
    fun find(code: String): StrictCutoffRollup?
    fun save(item: StrictCutoffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCutoffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCutoffRollupStore(
    var items: MutableMap<String, StrictCutoffRollup>,
    var order: MutableList<String>,
) : StrictCutoffRollupStore {
    override fun find(code: String): StrictCutoffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCutoffRollup) {
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

    override fun list(): MutableList<StrictCutoffRollup> {
        val out = mutableListOf<StrictCutoffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCutoffRollupStore(): StrictCutoffRollupStore {
    return MemoryStrictCutoffRollupStore(mutableMapOf<String, StrictCutoffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCutoffRollupStore(
    var inner: StrictCutoffRollupStore,
    var cache: TtlCache<StrictCutoffRollup>,
    var hits: Int,
    var misses: Int,
) : StrictCutoffRollupStore {
    override fun find(code: String): StrictCutoffRollup? {
        val hit = (cache.get(code) as StrictCutoffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCutoffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCutoffRollup> = inner.list()
}

fun newCachedStrictCutoffRollupStore(inner: StrictCutoffRollupStore, ttl: Int, clock: Clock): StrictCutoffRollupStore {
    return CachedStrictCutoffRollupStore(
        inner = inner,
        cache = newTtlCache<StrictCutoffRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict cutoff rollups. Empty fields match all. */
data class StrictCutoffRollupQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCutoffRollup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCutoffRollup>): MutableList<StrictCutoffRollup> {
        val matched = filter(items) { candidate: StrictCutoffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCutoffRollups(store: StrictCutoffRollupStore, query: StrictCutoffRollupQuery): MutableList<StrictCutoffRollup> {
    return query.apply(store.list())
}
