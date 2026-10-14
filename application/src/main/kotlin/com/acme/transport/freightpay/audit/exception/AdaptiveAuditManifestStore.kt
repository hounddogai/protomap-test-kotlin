package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit manifests by code. */
interface AdaptiveAuditManifestStore {
    fun find(code: String): AdaptiveAuditManifest?
    fun save(item: AdaptiveAuditManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditManifestStore(
    var items: MutableMap<String, AdaptiveAuditManifest>,
    var order: MutableList<String>,
) : AdaptiveAuditManifestStore {
    override fun find(code: String): AdaptiveAuditManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditManifest) {
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

    override fun list(): MutableList<AdaptiveAuditManifest> {
        val out = mutableListOf<AdaptiveAuditManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditManifestStore(): AdaptiveAuditManifestStore {
    return MemoryAdaptiveAuditManifestStore(
        mutableMapOf<String, AdaptiveAuditManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditManifestStore(
    var inner: AdaptiveAuditManifestStore,
    var cache: TtlCache<AdaptiveAuditManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditManifestStore {
    override fun find(code: String): AdaptiveAuditManifest? {
        val hit = (cache.get(code) as AdaptiveAuditManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditManifest> = inner.list()
}

fun newCachedAdaptiveAuditManifestStore(inner: AdaptiveAuditManifestStore, ttl: Int, clock: Clock): AdaptiveAuditManifestStore {
    return CachedAdaptiveAuditManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit manifests. Empty fields match all. */
data class AdaptiveAuditManifestQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditManifest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AdaptiveAuditManifest>): MutableList<AdaptiveAuditManifest> {
        val matched = filter(items) { candidate: AdaptiveAuditManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditManifests(store: AdaptiveAuditManifestStore, query: AdaptiveAuditManifestQuery): MutableList<AdaptiveAuditManifest> {
    return query.apply(store.list())
}
