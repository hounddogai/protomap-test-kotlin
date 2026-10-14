package com.acme.transport.crewing.availability.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability manifests by code. */
interface AvailabilityManifestStore {
    fun find(code: String): AvailabilityManifest?
    fun save(item: AvailabilityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityManifestStore(
    var items: MutableMap<String, AvailabilityManifest>,
    var order: MutableList<String>,
) : AvailabilityManifestStore {
    override fun find(code: String): AvailabilityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityManifest) {
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

    override fun list(): MutableList<AvailabilityManifest> {
        val out = mutableListOf<AvailabilityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityManifestStore(): AvailabilityManifestStore {
    return MemoryAvailabilityManifestStore(
        mutableMapOf<String, AvailabilityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityManifestStore(
    var inner: AvailabilityManifestStore,
    var cache: TtlCache<AvailabilityManifest>,
    var hits: Int,
    var misses: Int,
) : AvailabilityManifestStore {
    override fun find(code: String): AvailabilityManifest? {
        val hit = (cache.get(code) as AvailabilityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityManifest> = inner.list()
}

fun newCachedAvailabilityManifestStore(inner: AvailabilityManifestStore, ttl: Int, clock: Clock): AvailabilityManifestStore {
    return CachedAvailabilityManifestStore(
        inner = inner,
        cache = newTtlCache<AvailabilityManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability manifests. Empty fields match all. */
data class AvailabilityManifestQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityManifest>): MutableList<AvailabilityManifest> {
        val matched = filter(items) { candidate: AvailabilityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityManifests(store: AvailabilityManifestStore, query: AvailabilityManifestQuery): MutableList<AvailabilityManifest> {
    return query.apply(store.list())
}
