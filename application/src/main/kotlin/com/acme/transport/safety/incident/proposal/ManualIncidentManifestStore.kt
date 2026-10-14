package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual incident manifests by code. */
interface ManualIncidentManifestStore {
    fun find(code: String): ManualIncidentManifest?
    fun save(item: ManualIncidentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIncidentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIncidentManifestStore(
    var items: MutableMap<String, ManualIncidentManifest>,
    var order: MutableList<String>,
) : ManualIncidentManifestStore {
    override fun find(code: String): ManualIncidentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIncidentManifest) {
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

    override fun list(): MutableList<ManualIncidentManifest> {
        val out = mutableListOf<ManualIncidentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIncidentManifestStore(): ManualIncidentManifestStore {
    return MemoryManualIncidentManifestStore(
        mutableMapOf<String, ManualIncidentManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIncidentManifestStore(
    var inner: ManualIncidentManifestStore,
    var cache: TtlCache<ManualIncidentManifest>,
    var hits: Int,
    var misses: Int,
) : ManualIncidentManifestStore {
    override fun find(code: String): ManualIncidentManifest? {
        val hit = (cache.get(code) as ManualIncidentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIncidentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIncidentManifest> = inner.list()
}

fun newCachedManualIncidentManifestStore(inner: ManualIncidentManifestStore, ttl: Int, clock: Clock): ManualIncidentManifestStore {
    return CachedManualIncidentManifestStore(
        inner = inner,
        cache = newTtlCache<ManualIncidentManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual incident manifests. Empty fields match all. */
data class ManualIncidentManifestQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIncidentManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualIncidentManifest>): MutableList<ManualIncidentManifest> {
        val matched = filter(items) { candidate: ManualIncidentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIncidentManifests(store: ManualIncidentManifestStore, query: ManualIncidentManifestQuery): MutableList<ManualIncidentManifest> {
    return query.apply(store.list())
}
