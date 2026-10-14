package com.acme.transport.customs.broker.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered broker manifests by code. */
interface TieredBrokerManifestStore {
    fun find(code: String): TieredBrokerManifest?
    fun save(item: TieredBrokerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBrokerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBrokerManifestStore(
    var items: MutableMap<String, TieredBrokerManifest>,
    var order: MutableList<String>,
) : TieredBrokerManifestStore {
    override fun find(code: String): TieredBrokerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBrokerManifest) {
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

    override fun list(): MutableList<TieredBrokerManifest> {
        val out = mutableListOf<TieredBrokerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBrokerManifestStore(): TieredBrokerManifestStore {
    return MemoryTieredBrokerManifestStore(
        mutableMapOf<String, TieredBrokerManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBrokerManifestStore(
    var inner: TieredBrokerManifestStore,
    var cache: TtlCache<TieredBrokerManifest>,
    var hits: Int,
    var misses: Int,
) : TieredBrokerManifestStore {
    override fun find(code: String): TieredBrokerManifest? {
        val hit = (cache.get(code) as TieredBrokerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBrokerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBrokerManifest> = inner.list()
}

fun newCachedTieredBrokerManifestStore(inner: TieredBrokerManifestStore, ttl: Int, clock: Clock): TieredBrokerManifestStore {
    return CachedTieredBrokerManifestStore(
        inner = inner,
        cache = newTtlCache<TieredBrokerManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered broker manifests. Empty fields match all. */
data class TieredBrokerManifestQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBrokerManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredBrokerManifest>): MutableList<TieredBrokerManifest> {
        val matched = filter(items) { candidate: TieredBrokerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBrokerManifests(store: TieredBrokerManifestStore, query: TieredBrokerManifestQuery): MutableList<TieredBrokerManifest> {
    return query.apply(store.list())
}
