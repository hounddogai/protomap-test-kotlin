package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tariff variances by code. */
interface ManualTariffVarianceStore {
    fun find(code: String): ManualTariffVariance?
    fun save(item: ManualTariffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTariffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTariffVarianceStore(
    var items: MutableMap<String, ManualTariffVariance>,
    var order: MutableList<String>,
) : ManualTariffVarianceStore {
    override fun find(code: String): ManualTariffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTariffVariance) {
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

    override fun list(): MutableList<ManualTariffVariance> {
        val out = mutableListOf<ManualTariffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTariffVarianceStore(): ManualTariffVarianceStore {
    return MemoryManualTariffVarianceStore(
        mutableMapOf<String, ManualTariffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTariffVarianceStore(
    var inner: ManualTariffVarianceStore,
    var cache: TtlCache<ManualTariffVariance>,
    var hits: Int,
    var misses: Int,
) : ManualTariffVarianceStore {
    override fun find(code: String): ManualTariffVariance? {
        val hit = (cache.get(code) as ManualTariffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTariffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTariffVariance> = inner.list()
}

fun newCachedManualTariffVarianceStore(inner: ManualTariffVarianceStore, ttl: Int, clock: Clock): ManualTariffVarianceStore {
    return CachedManualTariffVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualTariffVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tariff variances. Empty fields match all. */
data class ManualTariffVarianceQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTariffVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTariffVariance>): MutableList<ManualTariffVariance> {
        val matched = filter(items) { candidate: ManualTariffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTariffVariances(store: ManualTariffVarianceStore, query: ManualTariffVarianceQuery): MutableList<ManualTariffVariance> {
    return query.apply(store.list())
}
