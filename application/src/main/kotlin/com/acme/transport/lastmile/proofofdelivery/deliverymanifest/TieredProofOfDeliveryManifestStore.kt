package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered proof of delivery manifests by code. */
interface TieredProofOfDeliveryManifestStore {
    fun find(code: String): TieredProofOfDeliveryManifest?
    fun save(item: TieredProofOfDeliveryManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredProofOfDeliveryManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredProofOfDeliveryManifestStore(
    var items: MutableMap<String, TieredProofOfDeliveryManifest>,
    var order: MutableList<String>,
) : TieredProofOfDeliveryManifestStore {
    override fun find(code: String): TieredProofOfDeliveryManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredProofOfDeliveryManifest) {
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

    override fun list(): MutableList<TieredProofOfDeliveryManifest> {
        val out = mutableListOf<TieredProofOfDeliveryManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredProofOfDeliveryManifestStore(): TieredProofOfDeliveryManifestStore {
    return MemoryTieredProofOfDeliveryManifestStore(
        mutableMapOf<String, TieredProofOfDeliveryManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredProofOfDeliveryManifestStore(
    var inner: TieredProofOfDeliveryManifestStore,
    var cache: TtlCache<TieredProofOfDeliveryManifest>,
    var hits: Int,
    var misses: Int,
) : TieredProofOfDeliveryManifestStore {
    override fun find(code: String): TieredProofOfDeliveryManifest? {
        val hit = (cache.get(code) as TieredProofOfDeliveryManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredProofOfDeliveryManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredProofOfDeliveryManifest> = inner.list()
}

fun newCachedTieredProofOfDeliveryManifestStore(inner: TieredProofOfDeliveryManifestStore, ttl: Int, clock: Clock): TieredProofOfDeliveryManifestStore {
    return CachedTieredProofOfDeliveryManifestStore(
        inner = inner,
        cache = newTtlCache<TieredProofOfDeliveryManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered proof of delivery manifests. Empty fields match all. */
data class TieredProofOfDeliveryManifestQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredProofOfDeliveryManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredProofOfDeliveryManifest>): MutableList<TieredProofOfDeliveryManifest> {
        val matched = filter(items) { candidate: TieredProofOfDeliveryManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredProofOfDeliveryManifests(store: TieredProofOfDeliveryManifestStore, query: TieredProofOfDeliveryManifestQuery): MutableList<TieredProofOfDeliveryManifest> {
    return query.apply(store.list())
}
