package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident manifests by code. */
interface IncidentManifestStore {
    fun find(code: String): IncidentManifest?
    fun save(item: IncidentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentManifestStore(
    var items: MutableMap<String, IncidentManifest>,
    var order: MutableList<String>,
) : IncidentManifestStore {
    override fun find(code: String): IncidentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentManifest) {
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

    override fun list(): MutableList<IncidentManifest> {
        val out = mutableListOf<IncidentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentManifestStore(): IncidentManifestStore {
    return MemoryIncidentManifestStore(mutableMapOf<String, IncidentManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentManifestStore(
    var inner: IncidentManifestStore,
    var cache: TtlCache<IncidentManifest>,
    var hits: Int,
    var misses: Int,
) : IncidentManifestStore {
    override fun find(code: String): IncidentManifest? {
        val hit = (cache.get(code) as IncidentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentManifest> = inner.list()
}

fun newCachedIncidentManifestStore(inner: IncidentManifestStore, ttl: Int, clock: Clock): IncidentManifestStore {
    return CachedIncidentManifestStore(
        inner = inner,
        cache = newTtlCache<IncidentManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident manifests. Empty fields match all. */
data class IncidentManifestQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentManifest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<IncidentManifest>): MutableList<IncidentManifest> {
        val matched = filter(items) { candidate: IncidentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentManifests(store: IncidentManifestStore, query: IncidentManifestQuery): MutableList<IncidentManifest> {
    return query.apply(store.list())
}
