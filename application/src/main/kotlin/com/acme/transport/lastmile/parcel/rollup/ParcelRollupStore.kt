package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel rollups by code. */
interface ParcelRollupStore {
    fun find(code: String): ParcelRollup?
    fun save(item: ParcelRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelRollupStore(
    var items: MutableMap<String, ParcelRollup>,
    var order: MutableList<String>,
) : ParcelRollupStore {
    override fun find(code: String): ParcelRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelRollup) {
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

    override fun list(): MutableList<ParcelRollup> {
        val out = mutableListOf<ParcelRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelRollupStore(): ParcelRollupStore {
    return MemoryParcelRollupStore(mutableMapOf<String, ParcelRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelRollupStore(
    var inner: ParcelRollupStore,
    var cache: TtlCache<ParcelRollup>,
    var hits: Int,
    var misses: Int,
) : ParcelRollupStore {
    override fun find(code: String): ParcelRollup? {
        val hit = (cache.get(code) as ParcelRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelRollup> = inner.list()
}

fun newCachedParcelRollupStore(inner: ParcelRollupStore, ttl: Int, clock: Clock): ParcelRollupStore {
    return CachedParcelRollupStore(
        inner = inner,
        cache = newTtlCache<ParcelRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel rollups. Empty fields match all. */
data class ParcelRollupQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelRollup>): MutableList<ParcelRollup> {
        val matched = filter(items) { candidate: ParcelRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelRollups(store: ParcelRollupStore, query: ParcelRollupQuery): MutableList<ParcelRollup> {
    return query.apply(store.list())
}
