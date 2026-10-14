package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional incident manifests by code. */
interface RegionalIncidentManifestStore {
    fun find(code: String): RegionalIncidentManifest?
    fun save(item: RegionalIncidentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIncidentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIncidentManifestStore(
    var items: MutableMap<String, RegionalIncidentManifest>,
    var order: MutableList<String>,
) : RegionalIncidentManifestStore {
    override fun find(code: String): RegionalIncidentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIncidentManifest) {
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

    override fun list(): MutableList<RegionalIncidentManifest> {
        val out = mutableListOf<RegionalIncidentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIncidentManifestStore(): RegionalIncidentManifestStore {
    return MemoryRegionalIncidentManifestStore(
        mutableMapOf<String, RegionalIncidentManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIncidentManifestStore(
    var inner: RegionalIncidentManifestStore,
    var cache: TtlCache<RegionalIncidentManifest>,
    var hits: Int,
    var misses: Int,
) : RegionalIncidentManifestStore {
    override fun find(code: String): RegionalIncidentManifest? {
        val hit = (cache.get(code) as RegionalIncidentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIncidentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIncidentManifest> = inner.list()
}

fun newCachedRegionalIncidentManifestStore(inner: RegionalIncidentManifestStore, ttl: Int, clock: Clock): RegionalIncidentManifestStore {
    return CachedRegionalIncidentManifestStore(
        inner = inner,
        cache = newTtlCache<RegionalIncidentManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional incident manifests. Empty fields match all. */
data class RegionalIncidentManifestQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIncidentManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIncidentManifest>): MutableList<RegionalIncidentManifest> {
        val matched = filter(items) { candidate: RegionalIncidentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIncidentManifests(store: RegionalIncidentManifestStore, query: RegionalIncidentManifestQuery): MutableList<RegionalIncidentManifest> {
    return query.apply(store.list())
}
