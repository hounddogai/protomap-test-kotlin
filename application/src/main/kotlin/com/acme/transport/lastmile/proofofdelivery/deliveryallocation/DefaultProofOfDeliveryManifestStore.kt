package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default proof of delivery manifests by code. */
interface DefaultProofOfDeliveryManifestStore {
    fun find(code: String): DefaultProofOfDeliveryManifest?
    fun save(item: DefaultProofOfDeliveryManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultProofOfDeliveryManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultProofOfDeliveryManifestStore(
    var items: MutableMap<String, DefaultProofOfDeliveryManifest>,
    var order: MutableList<String>,
) : DefaultProofOfDeliveryManifestStore {
    override fun find(code: String): DefaultProofOfDeliveryManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultProofOfDeliveryManifest) {
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

    override fun list(): MutableList<DefaultProofOfDeliveryManifest> {
        val out = mutableListOf<DefaultProofOfDeliveryManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultProofOfDeliveryManifestStore(): DefaultProofOfDeliveryManifestStore {
    return MemoryDefaultProofOfDeliveryManifestStore(
        mutableMapOf<String, DefaultProofOfDeliveryManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultProofOfDeliveryManifestStore(
    var inner: DefaultProofOfDeliveryManifestStore,
    var cache: TtlCache<DefaultProofOfDeliveryManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultProofOfDeliveryManifestStore {
    override fun find(code: String): DefaultProofOfDeliveryManifest? {
        val hit = (cache.get(code) as DefaultProofOfDeliveryManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultProofOfDeliveryManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultProofOfDeliveryManifest> = inner.list()
}

fun newCachedDefaultProofOfDeliveryManifestStore(inner: DefaultProofOfDeliveryManifestStore, ttl: Int, clock: Clock): DefaultProofOfDeliveryManifestStore {
    return CachedDefaultProofOfDeliveryManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultProofOfDeliveryManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default proof of delivery manifests. Empty fields match all. */
data class DefaultProofOfDeliveryManifestQuery(
    var unit: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultProofOfDeliveryManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultProofOfDeliveryManifest>): MutableList<DefaultProofOfDeliveryManifest> {
        val matched = filter(items) { candidate: DefaultProofOfDeliveryManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultProofOfDeliveryManifests(store: DefaultProofOfDeliveryManifestStore, query: DefaultProofOfDeliveryManifestQuery): MutableList<DefaultProofOfDeliveryManifest> {
    return query.apply(store.list())
}
