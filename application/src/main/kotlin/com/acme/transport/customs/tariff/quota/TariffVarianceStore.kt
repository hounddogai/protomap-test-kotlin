package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff variances by code. */
interface TariffVarianceStore {
    fun find(code: String): TariffVariance?
    fun save(item: TariffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffVarianceStore(
    var items: MutableMap<String, TariffVariance>,
    var order: MutableList<String>,
) : TariffVarianceStore {
    override fun find(code: String): TariffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffVariance) {
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

    override fun list(): MutableList<TariffVariance> {
        val out = mutableListOf<TariffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffVarianceStore(): TariffVarianceStore {
    return MemoryTariffVarianceStore(mutableMapOf<String, TariffVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffVarianceStore(
    var inner: TariffVarianceStore,
    var cache: TtlCache<TariffVariance>,
    var hits: Int,
    var misses: Int,
) : TariffVarianceStore {
    override fun find(code: String): TariffVariance? {
        val hit = (cache.get(code) as TariffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffVariance> = inner.list()
}

fun newCachedTariffVarianceStore(inner: TariffVarianceStore, ttl: Int, clock: Clock): TariffVarianceStore {
    return CachedTariffVarianceStore(
        inner = inner,
        cache = newTtlCache<TariffVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff variances. Empty fields match all. */
data class TariffVarianceQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TariffVariance>): MutableList<TariffVariance> {
        val matched = filter(items) { candidate: TariffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffVariances(store: TariffVarianceStore, query: TariffVarianceQuery): MutableList<TariffVariance> {
    return query.apply(store.list())
}
