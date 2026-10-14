package com.acme.transport.customs.tariff.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff cycles by code. */
interface TariffCycleStore {
    fun find(code: String): TariffCycle?
    fun save(item: TariffCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffCycleStore(
    var items: MutableMap<String, TariffCycle>,
    var order: MutableList<String>,
) : TariffCycleStore {
    override fun find(code: String): TariffCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffCycle) {
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

    override fun list(): MutableList<TariffCycle> {
        val out = mutableListOf<TariffCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffCycleStore(): TariffCycleStore {
    return MemoryTariffCycleStore(mutableMapOf<String, TariffCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffCycleStore(
    var inner: TariffCycleStore,
    var cache: TtlCache<TariffCycle>,
    var hits: Int,
    var misses: Int,
) : TariffCycleStore {
    override fun find(code: String): TariffCycle? {
        val hit = (cache.get(code) as TariffCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffCycle> = inner.list()
}

fun newCachedTariffCycleStore(inner: TariffCycleStore, ttl: Int, clock: Clock): TariffCycleStore {
    return CachedTariffCycleStore(
        inner = inner,
        cache = newTtlCache<TariffCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff cycles. Empty fields match all. */
data class TariffCycleQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffCycle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffCycle>): MutableList<TariffCycle> {
        val matched = filter(items) { candidate: TariffCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffCycles(store: TariffCycleStore, query: TariffCycleQuery): MutableList<TariffCycle> {
    return query.apply(store.list())
}
