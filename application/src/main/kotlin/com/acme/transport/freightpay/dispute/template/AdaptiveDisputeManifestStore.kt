package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive dispute manifests by code. */
interface AdaptiveDisputeManifestStore {
    fun find(code: String): AdaptiveDisputeManifest?
    fun save(item: AdaptiveDisputeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDisputeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDisputeManifestStore(
    var items: MutableMap<String, AdaptiveDisputeManifest>,
    var order: MutableList<String>,
) : AdaptiveDisputeManifestStore {
    override fun find(code: String): AdaptiveDisputeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDisputeManifest) {
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

    override fun list(): MutableList<AdaptiveDisputeManifest> {
        val out = mutableListOf<AdaptiveDisputeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDisputeManifestStore(): AdaptiveDisputeManifestStore {
    return MemoryAdaptiveDisputeManifestStore(
        mutableMapOf<String, AdaptiveDisputeManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDisputeManifestStore(
    var inner: AdaptiveDisputeManifestStore,
    var cache: TtlCache<AdaptiveDisputeManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDisputeManifestStore {
    override fun find(code: String): AdaptiveDisputeManifest? {
        val hit = (cache.get(code) as AdaptiveDisputeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDisputeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDisputeManifest> = inner.list()
}

fun newCachedAdaptiveDisputeManifestStore(inner: AdaptiveDisputeManifestStore, ttl: Int, clock: Clock): AdaptiveDisputeManifestStore {
    return CachedAdaptiveDisputeManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDisputeManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dispute manifests. Empty fields match all. */
data class AdaptiveDisputeManifestQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDisputeManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveDisputeManifest>): MutableList<AdaptiveDisputeManifest> {
        val matched = filter(items) { candidate: AdaptiveDisputeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDisputeManifests(store: AdaptiveDisputeManifestStore, query: AdaptiveDisputeManifestQuery): MutableList<AdaptiveDisputeManifest> {
    return query.apply(store.list())
}
