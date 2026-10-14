package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery manifests by code. */
interface RedeliveryManifestStore {
    fun find(code: String): RedeliveryManifest?
    fun save(item: RedeliveryManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryManifestStore(
    var items: MutableMap<String, RedeliveryManifest>,
    var order: MutableList<String>,
) : RedeliveryManifestStore {
    override fun find(code: String): RedeliveryManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryManifest) {
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

    override fun list(): MutableList<RedeliveryManifest> {
        val out = mutableListOf<RedeliveryManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryManifestStore(): RedeliveryManifestStore {
    return MemoryRedeliveryManifestStore(mutableMapOf<String, RedeliveryManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryManifestStore(
    var inner: RedeliveryManifestStore,
    var cache: TtlCache<RedeliveryManifest>,
    var hits: Int,
    var misses: Int,
) : RedeliveryManifestStore {
    override fun find(code: String): RedeliveryManifest? {
        val hit = (cache.get(code) as RedeliveryManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryManifest> = inner.list()
}

fun newCachedRedeliveryManifestStore(inner: RedeliveryManifestStore, ttl: Int, clock: Clock): RedeliveryManifestStore {
    return CachedRedeliveryManifestStore(
        inner = inner,
        cache = newTtlCache<RedeliveryManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery manifests. Empty fields match all. */
data class RedeliveryManifestQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryManifest>): MutableList<RedeliveryManifest> {
        val matched = filter(items) { candidate: RedeliveryManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryManifests(store: RedeliveryManifestStore, query: RedeliveryManifestQuery): MutableList<RedeliveryManifest> {
    return query.apply(store.list())
}
