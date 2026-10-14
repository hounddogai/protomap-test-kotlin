package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff windows by code. */
interface TariffWindowStore {
    fun find(code: String): TariffWindow?
    fun save(item: TariffWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffWindowStore(
    var items: MutableMap<String, TariffWindow>,
    var order: MutableList<String>,
) : TariffWindowStore {
    override fun find(code: String): TariffWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffWindow) {
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

    override fun list(): MutableList<TariffWindow> {
        val out = mutableListOf<TariffWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffWindowStore(): TariffWindowStore {
    return MemoryTariffWindowStore(mutableMapOf<String, TariffWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffWindowStore(
    var inner: TariffWindowStore,
    var cache: TtlCache<TariffWindow>,
    var hits: Int,
    var misses: Int,
) : TariffWindowStore {
    override fun find(code: String): TariffWindow? {
        val hit = (cache.get(code) as TariffWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffWindow> = inner.list()
}

fun newCachedTariffWindowStore(inner: TariffWindowStore, ttl: Int, clock: Clock): TariffWindowStore {
    return CachedTariffWindowStore(
        inner = inner,
        cache = newTtlCache<TariffWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff windows. Empty fields match all. */
data class TariffWindowQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffWindow>): MutableList<TariffWindow> {
        val matched = filter(items) { candidate: TariffWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffWindows(store: TariffWindowStore, query: TariffWindowQuery): MutableList<TariffWindow> {
    return query.apply(store.list())
}
