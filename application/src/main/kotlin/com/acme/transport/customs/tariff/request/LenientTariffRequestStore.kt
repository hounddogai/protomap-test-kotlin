package com.acme.transport.customs.tariff.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient tariff requests by code. */
interface LenientTariffRequestStore {
    fun find(code: String): LenientTariffRequest?
    fun save(item: LenientTariffRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTariffRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTariffRequestStore(
    var items: MutableMap<String, LenientTariffRequest>,
    var order: MutableList<String>,
) : LenientTariffRequestStore {
    override fun find(code: String): LenientTariffRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTariffRequest) {
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

    override fun list(): MutableList<LenientTariffRequest> {
        val out = mutableListOf<LenientTariffRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTariffRequestStore(): LenientTariffRequestStore {
    return MemoryLenientTariffRequestStore(
        mutableMapOf<String, LenientTariffRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTariffRequestStore(
    var inner: LenientTariffRequestStore,
    var cache: TtlCache<LenientTariffRequest>,
    var hits: Int,
    var misses: Int,
) : LenientTariffRequestStore {
    override fun find(code: String): LenientTariffRequest? {
        val hit = (cache.get(code) as LenientTariffRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTariffRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTariffRequest> = inner.list()
}

fun newCachedLenientTariffRequestStore(inner: LenientTariffRequestStore, ttl: Int, clock: Clock): LenientTariffRequestStore {
    return CachedLenientTariffRequestStore(
        inner = inner,
        cache = newTtlCache<LenientTariffRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient tariff requests. Empty fields match all. */
data class LenientTariffRequestQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTariffRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTariffRequest>): MutableList<LenientTariffRequest> {
        val matched = filter(items) { candidate: LenientTariffRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTariffRequests(store: LenientTariffRequestStore, query: LenientTariffRequestQuery): MutableList<LenientTariffRequest> {
    return query.apply(store.list())
}
