package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time manifests by code. */
interface TransitTimeManifestStore {
    fun find(code: String): TransitTimeManifest?
    fun save(item: TransitTimeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeManifestStore(
    var items: MutableMap<String, TransitTimeManifest>,
    var order: MutableList<String>,
) : TransitTimeManifestStore {
    override fun find(code: String): TransitTimeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeManifest) {
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

    override fun list(): MutableList<TransitTimeManifest> {
        val out = mutableListOf<TransitTimeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeManifestStore(): TransitTimeManifestStore {
    return MemoryTransitTimeManifestStore(mutableMapOf<String, TransitTimeManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeManifestStore(
    var inner: TransitTimeManifestStore,
    var cache: TtlCache<TransitTimeManifest>,
    var hits: Int,
    var misses: Int,
) : TransitTimeManifestStore {
    override fun find(code: String): TransitTimeManifest? {
        val hit = (cache.get(code) as TransitTimeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeManifest> = inner.list()
}

fun newCachedTransitTimeManifestStore(inner: TransitTimeManifestStore, ttl: Int, clock: Clock): TransitTimeManifestStore {
    return CachedTransitTimeManifestStore(
        inner = inner,
        cache = newTtlCache<TransitTimeManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time manifests. Empty fields match all. */
data class TransitTimeManifestQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeManifest>): MutableList<TransitTimeManifest> {
        val matched = filter(items) { candidate: TransitTimeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeManifests(store: TransitTimeManifestStore, query: TransitTimeManifestQuery): MutableList<TransitTimeManifest> {
    return query.apply(store.list())
}
