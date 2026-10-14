package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff digests by code. */
interface TariffDigestStore {
    fun find(code: String): TariffDigest?
    fun save(item: TariffDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffDigestStore(
    var items: MutableMap<String, TariffDigest>,
    var order: MutableList<String>,
) : TariffDigestStore {
    override fun find(code: String): TariffDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffDigest) {
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

    override fun list(): MutableList<TariffDigest> {
        val out = mutableListOf<TariffDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffDigestStore(): TariffDigestStore {
    return MemoryTariffDigestStore(mutableMapOf<String, TariffDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffDigestStore(
    var inner: TariffDigestStore,
    var cache: TtlCache<TariffDigest>,
    var hits: Int,
    var misses: Int,
) : TariffDigestStore {
    override fun find(code: String): TariffDigest? {
        val hit = (cache.get(code) as TariffDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffDigest> = inner.list()
}

fun newCachedTariffDigestStore(inner: TariffDigestStore, ttl: Int, clock: Clock): TariffDigestStore {
    return CachedTariffDigestStore(
        inner = inner,
        cache = newTtlCache<TariffDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff digests. Empty fields match all. */
data class TariffDigestQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffDigest>): MutableList<TariffDigest> {
        val matched = filter(items) { candidate: TariffDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffDigests(store: TariffDigestStore, query: TariffDigestQuery): MutableList<TariffDigest> {
    return query.apply(store.list())
}
