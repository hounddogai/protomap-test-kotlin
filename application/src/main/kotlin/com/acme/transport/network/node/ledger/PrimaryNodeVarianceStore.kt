package com.acme.transport.network.node.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary node variances by code. */
interface PrimaryNodeVarianceStore {
    fun find(code: String): PrimaryNodeVariance?
    fun save(item: PrimaryNodeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNodeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNodeVarianceStore(
    var items: MutableMap<String, PrimaryNodeVariance>,
    var order: MutableList<String>,
) : PrimaryNodeVarianceStore {
    override fun find(code: String): PrimaryNodeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNodeVariance) {
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

    override fun list(): MutableList<PrimaryNodeVariance> {
        val out = mutableListOf<PrimaryNodeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNodeVarianceStore(): PrimaryNodeVarianceStore {
    return MemoryPrimaryNodeVarianceStore(mutableMapOf<String, PrimaryNodeVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNodeVarianceStore(
    var inner: PrimaryNodeVarianceStore,
    var cache: TtlCache<PrimaryNodeVariance>,
    var hits: Int,
    var misses: Int,
) : PrimaryNodeVarianceStore {
    override fun find(code: String): PrimaryNodeVariance? {
        val hit = (cache.get(code) as PrimaryNodeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNodeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNodeVariance> = inner.list()
}

fun newCachedPrimaryNodeVarianceStore(inner: PrimaryNodeVarianceStore, ttl: Int, clock: Clock): PrimaryNodeVarianceStore {
    return CachedPrimaryNodeVarianceStore(
        inner = inner,
        cache = newTtlCache<PrimaryNodeVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary node variances. Empty fields match all. */
data class PrimaryNodeVarianceQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNodeVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryNodeVariance>): MutableList<PrimaryNodeVariance> {
        val matched = filter(items) { candidate: PrimaryNodeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNodeVariances(store: PrimaryNodeVarianceStore, query: PrimaryNodeVarianceQuery): MutableList<PrimaryNodeVariance> {
    return query.apply(store.list())
}
