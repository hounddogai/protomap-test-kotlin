package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient rate card manifests by code. */
interface LenientRateCardManifestStore {
    fun find(code: String): LenientRateCardManifest?
    fun save(item: LenientRateCardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRateCardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRateCardManifestStore(
    var items: MutableMap<String, LenientRateCardManifest>,
    var order: MutableList<String>,
) : LenientRateCardManifestStore {
    override fun find(code: String): LenientRateCardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRateCardManifest) {
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

    override fun list(): MutableList<LenientRateCardManifest> {
        val out = mutableListOf<LenientRateCardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRateCardManifestStore(): LenientRateCardManifestStore {
    return MemoryLenientRateCardManifestStore(
        mutableMapOf<String, LenientRateCardManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRateCardManifestStore(
    var inner: LenientRateCardManifestStore,
    var cache: TtlCache<LenientRateCardManifest>,
    var hits: Int,
    var misses: Int,
) : LenientRateCardManifestStore {
    override fun find(code: String): LenientRateCardManifest? {
        val hit = (cache.get(code) as LenientRateCardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRateCardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRateCardManifest> = inner.list()
}

fun newCachedLenientRateCardManifestStore(inner: LenientRateCardManifestStore, ttl: Int, clock: Clock): LenientRateCardManifestStore {
    return CachedLenientRateCardManifestStore(
        inner = inner,
        cache = newTtlCache<LenientRateCardManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient rate card manifests. Empty fields match all. */
data class LenientRateCardManifestQuery(
    var channel: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRateCardManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRateCardManifest>): MutableList<LenientRateCardManifest> {
        val matched = filter(items) { candidate: LenientRateCardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRateCardManifests(store: LenientRateCardManifestStore, query: LenientRateCardManifestQuery): MutableList<LenientRateCardManifest> {
    return query.apply(store.list())
}
