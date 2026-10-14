package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual clearance manifests by code. */
interface ManualClearanceManifestStore {
    fun find(code: String): ManualClearanceManifest?
    fun save(item: ManualClearanceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualClearanceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualClearanceManifestStore(
    var items: MutableMap<String, ManualClearanceManifest>,
    var order: MutableList<String>,
) : ManualClearanceManifestStore {
    override fun find(code: String): ManualClearanceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualClearanceManifest) {
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

    override fun list(): MutableList<ManualClearanceManifest> {
        val out = mutableListOf<ManualClearanceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualClearanceManifestStore(): ManualClearanceManifestStore {
    return MemoryManualClearanceManifestStore(
        mutableMapOf<String, ManualClearanceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualClearanceManifestStore(
    var inner: ManualClearanceManifestStore,
    var cache: TtlCache<ManualClearanceManifest>,
    var hits: Int,
    var misses: Int,
) : ManualClearanceManifestStore {
    override fun find(code: String): ManualClearanceManifest? {
        val hit = (cache.get(code) as ManualClearanceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualClearanceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualClearanceManifest> = inner.list()
}

fun newCachedManualClearanceManifestStore(inner: ManualClearanceManifestStore, ttl: Int, clock: Clock): ManualClearanceManifestStore {
    return CachedManualClearanceManifestStore(
        inner = inner,
        cache = newTtlCache<ManualClearanceManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual clearance manifests. Empty fields match all. */
data class ManualClearanceManifestQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualClearanceManifest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualClearanceManifest>): MutableList<ManualClearanceManifest> {
        val matched = filter(items) { candidate: ManualClearanceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualClearanceManifests(store: ManualClearanceManifestStore, query: ManualClearanceManifestQuery): MutableList<ManualClearanceManifest> {
    return query.apply(store.list())
}
