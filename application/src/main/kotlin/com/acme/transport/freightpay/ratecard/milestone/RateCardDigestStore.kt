package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card digests by code. */
interface RateCardDigestStore {
    fun find(code: String): RateCardDigest?
    fun save(item: RateCardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardDigestStore(
    var items: MutableMap<String, RateCardDigest>,
    var order: MutableList<String>,
) : RateCardDigestStore {
    override fun find(code: String): RateCardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardDigest) {
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

    override fun list(): MutableList<RateCardDigest> {
        val out = mutableListOf<RateCardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardDigestStore(): RateCardDigestStore {
    return MemoryRateCardDigestStore(mutableMapOf<String, RateCardDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardDigestStore(
    var inner: RateCardDigestStore,
    var cache: TtlCache<RateCardDigest>,
    var hits: Int,
    var misses: Int,
) : RateCardDigestStore {
    override fun find(code: String): RateCardDigest? {
        val hit = (cache.get(code) as RateCardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardDigest> = inner.list()
}

fun newCachedRateCardDigestStore(inner: RateCardDigestStore, ttl: Int, clock: Clock): RateCardDigestStore {
    return CachedRateCardDigestStore(
        inner = inner,
        cache = newTtlCache<RateCardDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card digests. Empty fields match all. */
data class RateCardDigestQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RateCardDigest>): MutableList<RateCardDigest> {
        val matched = filter(items) { candidate: RateCardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardDigests(store: RateCardDigestStore, query: RateCardDigestQuery): MutableList<RateCardDigest> {
    return query.apply(store.list())
}
