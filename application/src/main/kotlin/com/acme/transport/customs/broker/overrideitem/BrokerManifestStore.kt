package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker manifests by code. */
interface BrokerManifestStore {
    fun find(code: String): BrokerManifest?
    fun save(item: BrokerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerManifestStore(
    var items: MutableMap<String, BrokerManifest>,
    var order: MutableList<String>,
) : BrokerManifestStore {
    override fun find(code: String): BrokerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerManifest) {
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

    override fun list(): MutableList<BrokerManifest> {
        val out = mutableListOf<BrokerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerManifestStore(): BrokerManifestStore {
    return MemoryBrokerManifestStore(mutableMapOf<String, BrokerManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerManifestStore(
    var inner: BrokerManifestStore,
    var cache: TtlCache<BrokerManifest>,
    var hits: Int,
    var misses: Int,
) : BrokerManifestStore {
    override fun find(code: String): BrokerManifest? {
        val hit = (cache.get(code) as BrokerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerManifest> = inner.list()
}

fun newCachedBrokerManifestStore(inner: BrokerManifestStore, ttl: Int, clock: Clock): BrokerManifestStore {
    return CachedBrokerManifestStore(
        inner = inner,
        cache = newTtlCache<BrokerManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker manifests. Empty fields match all. */
data class BrokerManifestQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerManifest>): MutableList<BrokerManifest> {
        val matched = filter(items) { candidate: BrokerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerManifests(store: BrokerManifestStore, query: BrokerManifestQuery): MutableList<BrokerManifest> {
    return query.apply(store.list())
}
