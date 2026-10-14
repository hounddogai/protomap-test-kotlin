package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff groups by code. */
interface TariffGroupStore {
    fun find(code: String): TariffGroup?
    fun save(item: TariffGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffGroupStore(
    var items: MutableMap<String, TariffGroup>,
    var order: MutableList<String>,
) : TariffGroupStore {
    override fun find(code: String): TariffGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffGroup) {
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

    override fun list(): MutableList<TariffGroup> {
        val out = mutableListOf<TariffGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffGroupStore(): TariffGroupStore {
    return MemoryTariffGroupStore(mutableMapOf<String, TariffGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffGroupStore(
    var inner: TariffGroupStore,
    var cache: TtlCache<TariffGroup>,
    var hits: Int,
    var misses: Int,
) : TariffGroupStore {
    override fun find(code: String): TariffGroup? {
        val hit = (cache.get(code) as TariffGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffGroup> = inner.list()
}

fun newCachedTariffGroupStore(inner: TariffGroupStore, ttl: Int, clock: Clock): TariffGroupStore {
    return CachedTariffGroupStore(
        inner = inner,
        cache = newTtlCache<TariffGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff groups. Empty fields match all. */
data class TariffGroupQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffGroup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffGroup>): MutableList<TariffGroup> {
        val matched = filter(items) { candidate: TariffGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffGroups(store: TariffGroupStore, query: TariffGroupQuery): MutableList<TariffGroup> {
    return query.apply(store.list())
}
