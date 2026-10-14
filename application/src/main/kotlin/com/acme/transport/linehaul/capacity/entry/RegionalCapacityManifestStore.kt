package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity manifests by code. */
interface RegionalCapacityManifestStore {
    fun find(code: String): RegionalCapacityManifest?
    fun save(item: RegionalCapacityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityManifestStore(
    var items: MutableMap<String, RegionalCapacityManifest>,
    var order: MutableList<String>,
) : RegionalCapacityManifestStore {
    override fun find(code: String): RegionalCapacityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityManifest) {
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

    override fun list(): MutableList<RegionalCapacityManifest> {
        val out = mutableListOf<RegionalCapacityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityManifestStore(): RegionalCapacityManifestStore {
    return MemoryRegionalCapacityManifestStore(
        mutableMapOf<String, RegionalCapacityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityManifestStore(
    var inner: RegionalCapacityManifestStore,
    var cache: TtlCache<RegionalCapacityManifest>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityManifestStore {
    override fun find(code: String): RegionalCapacityManifest? {
        val hit = (cache.get(code) as RegionalCapacityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityManifest> = inner.list()
}

fun newCachedRegionalCapacityManifestStore(inner: RegionalCapacityManifestStore, ttl: Int, clock: Clock): RegionalCapacityManifestStore {
    return CachedRegionalCapacityManifestStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity manifests. Empty fields match all. */
data class RegionalCapacityManifestQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalCapacityManifest>): MutableList<RegionalCapacityManifest> {
        val matched = filter(items) { candidate: RegionalCapacityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityManifests(store: RegionalCapacityManifestStore, query: RegionalCapacityManifestQuery): MutableList<RegionalCapacityManifest> {
    return query.apply(store.list())
}
