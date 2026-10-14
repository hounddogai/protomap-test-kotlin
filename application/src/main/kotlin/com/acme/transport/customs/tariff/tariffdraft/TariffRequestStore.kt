package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff requests by code. */
interface TariffRequestStore {
    fun find(code: String): TariffRequest?
    fun save(item: TariffRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffRequestStore(
    var items: MutableMap<String, TariffRequest>,
    var order: MutableList<String>,
) : TariffRequestStore {
    override fun find(code: String): TariffRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffRequest) {
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

    override fun list(): MutableList<TariffRequest> {
        val out = mutableListOf<TariffRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffRequestStore(): TariffRequestStore {
    return MemoryTariffRequestStore(mutableMapOf<String, TariffRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffRequestStore(
    var inner: TariffRequestStore,
    var cache: TtlCache<TariffRequest>,
    var hits: Int,
    var misses: Int,
) : TariffRequestStore {
    override fun find(code: String): TariffRequest? {
        val hit = (cache.get(code) as TariffRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffRequest> = inner.list()
}

fun newCachedTariffRequestStore(inner: TariffRequestStore, ttl: Int, clock: Clock): TariffRequestStore {
    return CachedTariffRequestStore(
        inner = inner,
        cache = newTtlCache<TariffRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff requests. Empty fields match all. */
data class TariffRequestQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffRequest>): MutableList<TariffRequest> {
        val matched = filter(items) { candidate: TariffRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffRequests(store: TariffRequestStore, query: TariffRequestQuery): MutableList<TariffRequest> {
    return query.apply(store.list())
}
