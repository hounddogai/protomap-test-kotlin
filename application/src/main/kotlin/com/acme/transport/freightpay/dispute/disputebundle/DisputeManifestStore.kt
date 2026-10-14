package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute manifests by code. */
interface DisputeManifestStore {
    fun find(code: String): DisputeManifest?
    fun save(item: DisputeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeManifestStore(
    var items: MutableMap<String, DisputeManifest>,
    var order: MutableList<String>,
) : DisputeManifestStore {
    override fun find(code: String): DisputeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeManifest) {
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

    override fun list(): MutableList<DisputeManifest> {
        val out = mutableListOf<DisputeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeManifestStore(): DisputeManifestStore {
    return MemoryDisputeManifestStore(mutableMapOf<String, DisputeManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeManifestStore(
    var inner: DisputeManifestStore,
    var cache: TtlCache<DisputeManifest>,
    var hits: Int,
    var misses: Int,
) : DisputeManifestStore {
    override fun find(code: String): DisputeManifest? {
        val hit = (cache.get(code) as DisputeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeManifest> = inner.list()
}

fun newCachedDisputeManifestStore(inner: DisputeManifestStore, ttl: Int, clock: Clock): DisputeManifestStore {
    return CachedDisputeManifestStore(
        inner = inner,
        cache = newTtlCache<DisputeManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute manifests. Empty fields match all. */
data class DisputeManifestQuery(
    var region: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeManifest>): MutableList<DisputeManifest> {
        val matched = filter(items) { candidate: DisputeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeManifests(store: DisputeManifestStore, query: DisputeManifestQuery): MutableList<DisputeManifest> {
    return query.apply(store.list())
}
