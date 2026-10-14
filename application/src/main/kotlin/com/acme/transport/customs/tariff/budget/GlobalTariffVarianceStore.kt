package com.acme.transport.customs.tariff.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global tariff variances by code. */
interface GlobalTariffVarianceStore {
    fun find(code: String): GlobalTariffVariance?
    fun save(item: GlobalTariffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTariffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTariffVarianceStore(
    var items: MutableMap<String, GlobalTariffVariance>,
    var order: MutableList<String>,
) : GlobalTariffVarianceStore {
    override fun find(code: String): GlobalTariffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTariffVariance) {
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

    override fun list(): MutableList<GlobalTariffVariance> {
        val out = mutableListOf<GlobalTariffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTariffVarianceStore(): GlobalTariffVarianceStore {
    return MemoryGlobalTariffVarianceStore(
        mutableMapOf<String, GlobalTariffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTariffVarianceStore(
    var inner: GlobalTariffVarianceStore,
    var cache: TtlCache<GlobalTariffVariance>,
    var hits: Int,
    var misses: Int,
) : GlobalTariffVarianceStore {
    override fun find(code: String): GlobalTariffVariance? {
        val hit = (cache.get(code) as GlobalTariffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTariffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTariffVariance> = inner.list()
}

fun newCachedGlobalTariffVarianceStore(inner: GlobalTariffVarianceStore, ttl: Int, clock: Clock): GlobalTariffVarianceStore {
    return CachedGlobalTariffVarianceStore(
        inner = inner,
        cache = newTtlCache<GlobalTariffVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tariff variances. Empty fields match all. */
data class GlobalTariffVarianceQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTariffVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<GlobalTariffVariance>): MutableList<GlobalTariffVariance> {
        val matched = filter(items) { candidate: GlobalTariffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTariffVariances(store: GlobalTariffVarianceStore, query: GlobalTariffVarianceQuery): MutableList<GlobalTariffVariance> {
    return query.apply(store.list())
}
