package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection manifests by code. */
interface InspectionManifestStore {
    fun find(code: String): InspectionManifest?
    fun save(item: InspectionManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionManifestStore(
    var items: MutableMap<String, InspectionManifest>,
    var order: MutableList<String>,
) : InspectionManifestStore {
    override fun find(code: String): InspectionManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionManifest) {
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

    override fun list(): MutableList<InspectionManifest> {
        val out = mutableListOf<InspectionManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionManifestStore(): InspectionManifestStore {
    return MemoryInspectionManifestStore(mutableMapOf<String, InspectionManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionManifestStore(
    var inner: InspectionManifestStore,
    var cache: TtlCache<InspectionManifest>,
    var hits: Int,
    var misses: Int,
) : InspectionManifestStore {
    override fun find(code: String): InspectionManifest? {
        val hit = (cache.get(code) as InspectionManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionManifest> = inner.list()
}

fun newCachedInspectionManifestStore(inner: InspectionManifestStore, ttl: Int, clock: Clock): InspectionManifestStore {
    return CachedInspectionManifestStore(
        inner = inner,
        cache = newTtlCache<InspectionManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection manifests. Empty fields match all. */
data class InspectionManifestQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<InspectionManifest>): MutableList<InspectionManifest> {
        val matched = filter(items) { candidate: InspectionManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionManifests(store: InspectionManifestStore, query: InspectionManifestQuery): MutableList<InspectionManifest> {
    return query.apply(store.list())
}
