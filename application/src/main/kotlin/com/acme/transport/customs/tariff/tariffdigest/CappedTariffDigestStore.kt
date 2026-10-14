package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tariff digests by code. */
interface CappedTariffDigestStore {
    fun find(code: String): CappedTariffDigest?
    fun save(item: CappedTariffDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTariffDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTariffDigestStore(
    var items: MutableMap<String, CappedTariffDigest>,
    var order: MutableList<String>,
) : CappedTariffDigestStore {
    override fun find(code: String): CappedTariffDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTariffDigest) {
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

    override fun list(): MutableList<CappedTariffDigest> {
        val out = mutableListOf<CappedTariffDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTariffDigestStore(): CappedTariffDigestStore {
    return MemoryCappedTariffDigestStore(mutableMapOf<String, CappedTariffDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTariffDigestStore(
    var inner: CappedTariffDigestStore,
    var cache: TtlCache<CappedTariffDigest>,
    var hits: Int,
    var misses: Int,
) : CappedTariffDigestStore {
    override fun find(code: String): CappedTariffDigest? {
        val hit = (cache.get(code) as CappedTariffDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTariffDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTariffDigest> = inner.list()
}

fun newCachedCappedTariffDigestStore(inner: CappedTariffDigestStore, ttl: Int, clock: Clock): CappedTariffDigestStore {
    return CachedCappedTariffDigestStore(
        inner = inner,
        cache = newTtlCache<CappedTariffDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tariff digests. Empty fields match all. */
data class CappedTariffDigestQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTariffDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CappedTariffDigest>): MutableList<CappedTariffDigest> {
        val matched = filter(items) { candidate: CappedTariffDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTariffDigests(store: CappedTariffDigestStore, query: CappedTariffDigestQuery): MutableList<CappedTariffDigest> {
    return query.apply(store.list())
}
