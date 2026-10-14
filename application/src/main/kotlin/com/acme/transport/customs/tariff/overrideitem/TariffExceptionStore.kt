package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff exceptions by code. */
interface TariffExceptionStore {
    fun find(code: String): TariffException?
    fun save(item: TariffException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffExceptionStore(
    var items: MutableMap<String, TariffException>,
    var order: MutableList<String>,
) : TariffExceptionStore {
    override fun find(code: String): TariffException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffException) {
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

    override fun list(): MutableList<TariffException> {
        val out = mutableListOf<TariffException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffExceptionStore(): TariffExceptionStore {
    return MemoryTariffExceptionStore(mutableMapOf<String, TariffException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffExceptionStore(
    var inner: TariffExceptionStore,
    var cache: TtlCache<TariffException>,
    var hits: Int,
    var misses: Int,
) : TariffExceptionStore {
    override fun find(code: String): TariffException? {
        val hit = (cache.get(code) as TariffException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffException> = inner.list()
}

fun newCachedTariffExceptionStore(inner: TariffExceptionStore, ttl: Int, clock: Clock): TariffExceptionStore {
    return CachedTariffExceptionStore(
        inner = inner,
        cache = newTtlCache<TariffException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff exceptions. Empty fields match all. */
data class TariffExceptionQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TariffException>): MutableList<TariffException> {
        val matched = filter(items) { candidate: TariffException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffExceptions(store: TariffExceptionStore, query: TariffExceptionQuery): MutableList<TariffException> {
    return query.apply(store.list())
}
