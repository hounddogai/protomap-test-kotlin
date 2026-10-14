package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy incident manifests by code. */
interface LegacyIncidentManifestStore {
    fun find(code: String): LegacyIncidentManifest?
    fun save(item: LegacyIncidentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIncidentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIncidentManifestStore(
    var items: MutableMap<String, LegacyIncidentManifest>,
    var order: MutableList<String>,
) : LegacyIncidentManifestStore {
    override fun find(code: String): LegacyIncidentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIncidentManifest) {
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

    override fun list(): MutableList<LegacyIncidentManifest> {
        val out = mutableListOf<LegacyIncidentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIncidentManifestStore(): LegacyIncidentManifestStore {
    return MemoryLegacyIncidentManifestStore(
        mutableMapOf<String, LegacyIncidentManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIncidentManifestStore(
    var inner: LegacyIncidentManifestStore,
    var cache: TtlCache<LegacyIncidentManifest>,
    var hits: Int,
    var misses: Int,
) : LegacyIncidentManifestStore {
    override fun find(code: String): LegacyIncidentManifest? {
        val hit = (cache.get(code) as LegacyIncidentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIncidentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIncidentManifest> = inner.list()
}

fun newCachedLegacyIncidentManifestStore(inner: LegacyIncidentManifestStore, ttl: Int, clock: Clock): LegacyIncidentManifestStore {
    return CachedLegacyIncidentManifestStore(
        inner = inner,
        cache = newTtlCache<LegacyIncidentManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy incident manifests. Empty fields match all. */
data class LegacyIncidentManifestQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIncidentManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIncidentManifest>): MutableList<LegacyIncidentManifest> {
        val matched = filter(items) { candidate: LegacyIncidentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIncidentManifests(store: LegacyIncidentManifestStore, query: LegacyIncidentManifestQuery): MutableList<LegacyIncidentManifest> {
    return query.apply(store.list())
}
