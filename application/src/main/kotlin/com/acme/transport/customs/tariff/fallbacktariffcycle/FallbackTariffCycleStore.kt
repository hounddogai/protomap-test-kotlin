package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback tariff cycles by code. */
interface FallbackTariffCycleStore {
    fun find(code: String): FallbackTariffCycle?
    fun save(item: FallbackTariffCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTariffCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTariffCycleStore(
    var items: MutableMap<String, FallbackTariffCycle>,
    var order: MutableList<String>,
) : FallbackTariffCycleStore {
    override fun find(code: String): FallbackTariffCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTariffCycle) {
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

    override fun list(): MutableList<FallbackTariffCycle> {
        val out = mutableListOf<FallbackTariffCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTariffCycleStore(): FallbackTariffCycleStore {
    return MemoryFallbackTariffCycleStore(mutableMapOf<String, FallbackTariffCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTariffCycleStore(
    var inner: FallbackTariffCycleStore,
    var cache: TtlCache<FallbackTariffCycle>,
    var hits: Int,
    var misses: Int,
) : FallbackTariffCycleStore {
    override fun find(code: String): FallbackTariffCycle? {
        val hit = (cache.get(code) as FallbackTariffCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTariffCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTariffCycle> = inner.list()
}

fun newCachedFallbackTariffCycleStore(inner: FallbackTariffCycleStore, ttl: Int, clock: Clock): FallbackTariffCycleStore {
    return CachedFallbackTariffCycleStore(
        inner = inner,
        cache = newTtlCache<FallbackTariffCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tariff cycles. Empty fields match all. */
data class FallbackTariffCycleQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTariffCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTariffCycle>): MutableList<FallbackTariffCycle> {
        val matched = filter(items) { candidate: FallbackTariffCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTariffCycles(store: FallbackTariffCycleStore, query: FallbackTariffCycleQuery): MutableList<FallbackTariffCycle> {
    return query.apply(store.list())
}
