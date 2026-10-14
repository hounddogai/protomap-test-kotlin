package com.acme.transport.fleetops.tire.slot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire baselines by code. */
interface TireBaselineStore {
    fun find(code: String): TireBaseline?
    fun save(item: TireBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireBaselineStore(
    var items: MutableMap<String, TireBaseline>,
    var order: MutableList<String>,
) : TireBaselineStore {
    override fun find(code: String): TireBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireBaseline) {
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

    override fun list(): MutableList<TireBaseline> {
        val out = mutableListOf<TireBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireBaselineStore(): TireBaselineStore {
    return MemoryTireBaselineStore(mutableMapOf<String, TireBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireBaselineStore(
    var inner: TireBaselineStore,
    var cache: TtlCache<TireBaseline>,
    var hits: Int,
    var misses: Int,
) : TireBaselineStore {
    override fun find(code: String): TireBaseline? {
        val hit = (cache.get(code) as TireBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireBaseline> = inner.list()
}

fun newCachedTireBaselineStore(inner: TireBaselineStore, ttl: Int, clock: Clock): TireBaselineStore {
    return CachedTireBaselineStore(
        inner = inner,
        cache = newTtlCache<TireBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire baselines. Empty fields match all. */
data class TireBaselineQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireBaseline>): MutableList<TireBaseline> {
        val matched = filter(items) { candidate: TireBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireBaselines(store: TireBaselineStore, query: TireBaselineQuery): MutableList<TireBaseline> {
    return query.apply(store.list())
}
