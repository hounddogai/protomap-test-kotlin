package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy lane plan manifests by code. */
interface LegacyLanePlanManifestStore {
    fun find(code: String): LegacyLanePlanManifest?
    fun save(item: LegacyLanePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLanePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLanePlanManifestStore(
    var items: MutableMap<String, LegacyLanePlanManifest>,
    var order: MutableList<String>,
) : LegacyLanePlanManifestStore {
    override fun find(code: String): LegacyLanePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLanePlanManifest) {
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

    override fun list(): MutableList<LegacyLanePlanManifest> {
        val out = mutableListOf<LegacyLanePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLanePlanManifestStore(): LegacyLanePlanManifestStore {
    return MemoryLegacyLanePlanManifestStore(
        mutableMapOf<String, LegacyLanePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLanePlanManifestStore(
    var inner: LegacyLanePlanManifestStore,
    var cache: TtlCache<LegacyLanePlanManifest>,
    var hits: Int,
    var misses: Int,
) : LegacyLanePlanManifestStore {
    override fun find(code: String): LegacyLanePlanManifest? {
        val hit = (cache.get(code) as LegacyLanePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLanePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLanePlanManifest> = inner.list()
}

fun newCachedLegacyLanePlanManifestStore(inner: LegacyLanePlanManifestStore, ttl: Int, clock: Clock): LegacyLanePlanManifestStore {
    return CachedLegacyLanePlanManifestStore(
        inner = inner,
        cache = newTtlCache<LegacyLanePlanManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lane plan manifests. Empty fields match all. */
data class LegacyLanePlanManifestQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLanePlanManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLanePlanManifest>): MutableList<LegacyLanePlanManifest> {
        val matched = filter(items) { candidate: LegacyLanePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLanePlanManifests(store: LegacyLanePlanManifestStore, query: LegacyLanePlanManifestQuery): MutableList<LegacyLanePlanManifest> {
    return query.apply(store.list())
}
