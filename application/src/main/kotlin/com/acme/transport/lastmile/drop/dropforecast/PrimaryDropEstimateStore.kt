package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary drop estimates by code. */
interface PrimaryDropEstimateStore {
    fun find(code: String): PrimaryDropEstimate?
    fun save(item: PrimaryDropEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDropEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDropEstimateStore(
    var items: MutableMap<String, PrimaryDropEstimate>,
    var order: MutableList<String>,
) : PrimaryDropEstimateStore {
    override fun find(code: String): PrimaryDropEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDropEstimate) {
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

    override fun list(): MutableList<PrimaryDropEstimate> {
        val out = mutableListOf<PrimaryDropEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDropEstimateStore(): PrimaryDropEstimateStore {
    return MemoryPrimaryDropEstimateStore(mutableMapOf<String, PrimaryDropEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDropEstimateStore(
    var inner: PrimaryDropEstimateStore,
    var cache: TtlCache<PrimaryDropEstimate>,
    var hits: Int,
    var misses: Int,
) : PrimaryDropEstimateStore {
    override fun find(code: String): PrimaryDropEstimate? {
        val hit = (cache.get(code) as PrimaryDropEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDropEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDropEstimate> = inner.list()
}

fun newCachedPrimaryDropEstimateStore(inner: PrimaryDropEstimateStore, ttl: Int, clock: Clock): PrimaryDropEstimateStore {
    return CachedPrimaryDropEstimateStore(
        inner = inner,
        cache = newTtlCache<PrimaryDropEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary drop estimates. Empty fields match all. */
data class PrimaryDropEstimateQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDropEstimate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<PrimaryDropEstimate>): MutableList<PrimaryDropEstimate> {
        val matched = filter(items) { candidate: PrimaryDropEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDropEstimates(store: PrimaryDropEstimateStore, query: PrimaryDropEstimateQuery): MutableList<PrimaryDropEstimate> {
    return query.apply(store.list())
}
