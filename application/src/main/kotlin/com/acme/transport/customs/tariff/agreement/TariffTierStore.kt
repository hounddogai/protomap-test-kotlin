package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff tiers by code. */
interface TariffTierStore {
    fun find(code: String): TariffTier?
    fun save(item: TariffTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffTierStore(
    var items: MutableMap<String, TariffTier>,
    var order: MutableList<String>,
) : TariffTierStore {
    override fun find(code: String): TariffTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffTier) {
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

    override fun list(): MutableList<TariffTier> {
        val out = mutableListOf<TariffTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffTierStore(): TariffTierStore {
    return MemoryTariffTierStore(mutableMapOf<String, TariffTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffTierStore(
    var inner: TariffTierStore,
    var cache: TtlCache<TariffTier>,
    var hits: Int,
    var misses: Int,
) : TariffTierStore {
    override fun find(code: String): TariffTier? {
        val hit = (cache.get(code) as TariffTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffTier> = inner.list()
}

fun newCachedTariffTierStore(inner: TariffTierStore, ttl: Int, clock: Clock): TariffTierStore {
    return CachedTariffTierStore(
        inner = inner,
        cache = newTtlCache<TariffTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff tiers. Empty fields match all. */
data class TariffTierQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffTier): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<TariffTier>): MutableList<TariffTier> {
        val matched = filter(items) { candidate: TariffTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffTiers(store: TariffTierStore, query: TariffTierQuery): MutableList<TariffTier> {
    return query.apply(store.list())
}
