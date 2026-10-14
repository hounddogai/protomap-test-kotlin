package com.acme.transport.network.node.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped node variances by code. */
interface CappedNodeVarianceStore {
    fun find(code: String): CappedNodeVariance?
    fun save(item: CappedNodeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNodeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNodeVarianceStore(
    var items: MutableMap<String, CappedNodeVariance>,
    var order: MutableList<String>,
) : CappedNodeVarianceStore {
    override fun find(code: String): CappedNodeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNodeVariance) {
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

    override fun list(): MutableList<CappedNodeVariance> {
        val out = mutableListOf<CappedNodeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNodeVarianceStore(): CappedNodeVarianceStore {
    return MemoryCappedNodeVarianceStore(mutableMapOf<String, CappedNodeVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNodeVarianceStore(
    var inner: CappedNodeVarianceStore,
    var cache: TtlCache<CappedNodeVariance>,
    var hits: Int,
    var misses: Int,
) : CappedNodeVarianceStore {
    override fun find(code: String): CappedNodeVariance? {
        val hit = (cache.get(code) as CappedNodeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNodeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNodeVariance> = inner.list()
}

fun newCachedCappedNodeVarianceStore(inner: CappedNodeVarianceStore, ttl: Int, clock: Clock): CappedNodeVarianceStore {
    return CachedCappedNodeVarianceStore(
        inner = inner,
        cache = newTtlCache<CappedNodeVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped node variances. Empty fields match all. */
data class CappedNodeVarianceQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNodeVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedNodeVariance>): MutableList<CappedNodeVariance> {
        val matched = filter(items) { candidate: CappedNodeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNodeVariances(store: CappedNodeVarianceStore, query: CappedNodeVarianceQuery): MutableList<CappedNodeVariance> {
    return query.apply(store.list())
}
