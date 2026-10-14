package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff rollups by code. */
interface TariffRollupStore {
    fun find(code: String): TariffRollup?
    fun save(item: TariffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffRollupStore(
    var items: MutableMap<String, TariffRollup>,
    var order: MutableList<String>,
) : TariffRollupStore {
    override fun find(code: String): TariffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffRollup) {
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

    override fun list(): MutableList<TariffRollup> {
        val out = mutableListOf<TariffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffRollupStore(): TariffRollupStore {
    return MemoryTariffRollupStore(mutableMapOf<String, TariffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffRollupStore(
    var inner: TariffRollupStore,
    var cache: TtlCache<TariffRollup>,
    var hits: Int,
    var misses: Int,
) : TariffRollupStore {
    override fun find(code: String): TariffRollup? {
        val hit = (cache.get(code) as TariffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffRollup> = inner.list()
}

fun newCachedTariffRollupStore(inner: TariffRollupStore, ttl: Int, clock: Clock): TariffRollupStore {
    return CachedTariffRollupStore(
        inner = inner,
        cache = newTtlCache<TariffRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff rollups. Empty fields match all. */
data class TariffRollupQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffRollup>): MutableList<TariffRollup> {
        val matched = filter(items) { candidate: TariffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffRollups(store: TariffRollupStore, query: TariffRollupQuery): MutableList<TariffRollup> {
    return query.apply(store.list())
}
