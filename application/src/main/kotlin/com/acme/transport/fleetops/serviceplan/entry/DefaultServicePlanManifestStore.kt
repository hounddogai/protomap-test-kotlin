package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default service plan manifests by code. */
interface DefaultServicePlanManifestStore {
    fun find(code: String): DefaultServicePlanManifest?
    fun save(item: DefaultServicePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServicePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServicePlanManifestStore(
    var items: MutableMap<String, DefaultServicePlanManifest>,
    var order: MutableList<String>,
) : DefaultServicePlanManifestStore {
    override fun find(code: String): DefaultServicePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServicePlanManifest) {
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

    override fun list(): MutableList<DefaultServicePlanManifest> {
        val out = mutableListOf<DefaultServicePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServicePlanManifestStore(): DefaultServicePlanManifestStore {
    return MemoryDefaultServicePlanManifestStore(
        mutableMapOf<String, DefaultServicePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServicePlanManifestStore(
    var inner: DefaultServicePlanManifestStore,
    var cache: TtlCache<DefaultServicePlanManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultServicePlanManifestStore {
    override fun find(code: String): DefaultServicePlanManifest? {
        val hit = (cache.get(code) as DefaultServicePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServicePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServicePlanManifest> = inner.list()
}

fun newCachedDefaultServicePlanManifestStore(inner: DefaultServicePlanManifestStore, ttl: Int, clock: Clock): DefaultServicePlanManifestStore {
    return CachedDefaultServicePlanManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultServicePlanManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service plan manifests. Empty fields match all. */
data class DefaultServicePlanManifestQuery(
    var segment: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServicePlanManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServicePlanManifest>): MutableList<DefaultServicePlanManifest> {
        val matched = filter(items) { candidate: DefaultServicePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServicePlanManifests(store: DefaultServicePlanManifestStore, query: DefaultServicePlanManifestQuery): MutableList<DefaultServicePlanManifest> {
    return query.apply(store.list())
}
