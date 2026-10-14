package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default idling variances by code. */
interface DefaultIdlingVarianceStore {
    fun find(code: String): DefaultIdlingVariance?
    fun save(item: DefaultIdlingVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultIdlingVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultIdlingVarianceStore(
    var items: MutableMap<String, DefaultIdlingVariance>,
    var order: MutableList<String>,
) : DefaultIdlingVarianceStore {
    override fun find(code: String): DefaultIdlingVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultIdlingVariance) {
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

    override fun list(): MutableList<DefaultIdlingVariance> {
        val out = mutableListOf<DefaultIdlingVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultIdlingVarianceStore(): DefaultIdlingVarianceStore {
    return MemoryDefaultIdlingVarianceStore(
        mutableMapOf<String, DefaultIdlingVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultIdlingVarianceStore(
    var inner: DefaultIdlingVarianceStore,
    var cache: TtlCache<DefaultIdlingVariance>,
    var hits: Int,
    var misses: Int,
) : DefaultIdlingVarianceStore {
    override fun find(code: String): DefaultIdlingVariance? {
        val hit = (cache.get(code) as DefaultIdlingVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultIdlingVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultIdlingVariance> = inner.list()
}

fun newCachedDefaultIdlingVarianceStore(inner: DefaultIdlingVarianceStore, ttl: Int, clock: Clock): DefaultIdlingVarianceStore {
    return CachedDefaultIdlingVarianceStore(
        inner = inner,
        cache = newTtlCache<DefaultIdlingVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default idling variances. Empty fields match all. */
data class DefaultIdlingVarianceQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultIdlingVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultIdlingVariance>): MutableList<DefaultIdlingVariance> {
        val matched = filter(items) { candidate: DefaultIdlingVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultIdlingVariances(store: DefaultIdlingVarianceStore, query: DefaultIdlingVarianceQuery): MutableList<DefaultIdlingVariance> {
    return query.apply(store.list())
}
