package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff thresholds by code. */
interface TariffThresholdStore {
    fun find(code: String): TariffThreshold?
    fun save(item: TariffThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffThresholdStore(
    var items: MutableMap<String, TariffThreshold>,
    var order: MutableList<String>,
) : TariffThresholdStore {
    override fun find(code: String): TariffThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffThreshold) {
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

    override fun list(): MutableList<TariffThreshold> {
        val out = mutableListOf<TariffThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffThresholdStore(): TariffThresholdStore {
    return MemoryTariffThresholdStore(mutableMapOf<String, TariffThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffThresholdStore(
    var inner: TariffThresholdStore,
    var cache: TtlCache<TariffThreshold>,
    var hits: Int,
    var misses: Int,
) : TariffThresholdStore {
    override fun find(code: String): TariffThreshold? {
        val hit = (cache.get(code) as TariffThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffThreshold> = inner.list()
}

fun newCachedTariffThresholdStore(inner: TariffThresholdStore, ttl: Int, clock: Clock): TariffThresholdStore {
    return CachedTariffThresholdStore(
        inner = inner,
        cache = newTtlCache<TariffThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff thresholds. Empty fields match all. */
data class TariffThresholdQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffThreshold): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffThreshold>): MutableList<TariffThreshold> {
        val matched = filter(items) { candidate: TariffThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffThresholds(store: TariffThresholdStore, query: TariffThresholdQuery): MutableList<TariffThreshold> {
    return query.apply(store.list())
}
