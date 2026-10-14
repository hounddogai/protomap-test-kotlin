package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tariff exceptions by code. */
interface PrimaryTariffExceptionStore {
    fun find(code: String): PrimaryTariffException?
    fun save(item: PrimaryTariffException)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTariffException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTariffExceptionStore(
    var items: MutableMap<String, PrimaryTariffException>,
    var order: MutableList<String>,
) : PrimaryTariffExceptionStore {
    override fun find(code: String): PrimaryTariffException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTariffException) {
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

    override fun list(): MutableList<PrimaryTariffException> {
        val out = mutableListOf<PrimaryTariffException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTariffExceptionStore(): PrimaryTariffExceptionStore {
    return MemoryPrimaryTariffExceptionStore(
        mutableMapOf<String, PrimaryTariffException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTariffExceptionStore(
    var inner: PrimaryTariffExceptionStore,
    var cache: TtlCache<PrimaryTariffException>,
    var hits: Int,
    var misses: Int,
) : PrimaryTariffExceptionStore {
    override fun find(code: String): PrimaryTariffException? {
        val hit = (cache.get(code) as PrimaryTariffException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTariffException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTariffException> = inner.list()
}

fun newCachedPrimaryTariffExceptionStore(inner: PrimaryTariffExceptionStore, ttl: Int, clock: Clock): PrimaryTariffExceptionStore {
    return CachedPrimaryTariffExceptionStore(
        inner = inner,
        cache = newTtlCache<PrimaryTariffException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tariff exceptions. Empty fields match all. */
data class PrimaryTariffExceptionQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTariffException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTariffException>): MutableList<PrimaryTariffException> {
        val matched = filter(items) { candidate: PrimaryTariffException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTariffExceptions(store: PrimaryTariffExceptionStore, query: PrimaryTariffExceptionQuery): MutableList<PrimaryTariffException> {
    return query.apply(store.list())
}
