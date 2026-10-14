package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global service plan manifests by code. */
interface GlobalServicePlanManifestStore {
    fun find(code: String): GlobalServicePlanManifest?
    fun save(item: GlobalServicePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServicePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServicePlanManifestStore(
    var items: MutableMap<String, GlobalServicePlanManifest>,
    var order: MutableList<String>,
) : GlobalServicePlanManifestStore {
    override fun find(code: String): GlobalServicePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServicePlanManifest) {
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

    override fun list(): MutableList<GlobalServicePlanManifest> {
        val out = mutableListOf<GlobalServicePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServicePlanManifestStore(): GlobalServicePlanManifestStore {
    return MemoryGlobalServicePlanManifestStore(
        mutableMapOf<String, GlobalServicePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServicePlanManifestStore(
    var inner: GlobalServicePlanManifestStore,
    var cache: TtlCache<GlobalServicePlanManifest>,
    var hits: Int,
    var misses: Int,
) : GlobalServicePlanManifestStore {
    override fun find(code: String): GlobalServicePlanManifest? {
        val hit = (cache.get(code) as GlobalServicePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServicePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServicePlanManifest> = inner.list()
}

fun newCachedGlobalServicePlanManifestStore(inner: GlobalServicePlanManifestStore, ttl: Int, clock: Clock): GlobalServicePlanManifestStore {
    return CachedGlobalServicePlanManifestStore(
        inner = inner,
        cache = newTtlCache<GlobalServicePlanManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service plan manifests. Empty fields match all. */
data class GlobalServicePlanManifestQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServicePlanManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalServicePlanManifest>): MutableList<GlobalServicePlanManifest> {
        val matched = filter(items) { candidate: GlobalServicePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServicePlanManifests(store: GlobalServicePlanManifestStore, query: GlobalServicePlanManifestQuery): MutableList<GlobalServicePlanManifest> {
    return query.apply(store.list())
}
