package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card manifests by code. */
interface RateCardManifestStore {
    fun find(code: String): RateCardManifest?
    fun save(item: RateCardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardManifestStore(
    var items: MutableMap<String, RateCardManifest>,
    var order: MutableList<String>,
) : RateCardManifestStore {
    override fun find(code: String): RateCardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardManifest) {
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

    override fun list(): MutableList<RateCardManifest> {
        val out = mutableListOf<RateCardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardManifestStore(): RateCardManifestStore {
    return MemoryRateCardManifestStore(mutableMapOf<String, RateCardManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardManifestStore(
    var inner: RateCardManifestStore,
    var cache: TtlCache<RateCardManifest>,
    var hits: Int,
    var misses: Int,
) : RateCardManifestStore {
    override fun find(code: String): RateCardManifest? {
        val hit = (cache.get(code) as RateCardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardManifest> = inner.list()
}

fun newCachedRateCardManifestStore(inner: RateCardManifestStore, ttl: Int, clock: Clock): RateCardManifestStore {
    return CachedRateCardManifestStore(
        inner = inner,
        cache = newTtlCache<RateCardManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card manifests. Empty fields match all. */
data class RateCardManifestQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardManifest>): MutableList<RateCardManifest> {
        val matched = filter(items) { candidate: RateCardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardManifests(store: RateCardManifestStore, query: RateCardManifestQuery): MutableList<RateCardManifest> {
    return query.apply(store.list())
}
