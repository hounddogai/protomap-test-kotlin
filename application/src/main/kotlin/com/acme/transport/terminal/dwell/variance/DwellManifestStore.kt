package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell manifests by code. */
interface DwellManifestStore {
    fun find(code: String): DwellManifest?
    fun save(item: DwellManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellManifestStore(
    var items: MutableMap<String, DwellManifest>,
    var order: MutableList<String>,
) : DwellManifestStore {
    override fun find(code: String): DwellManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellManifest) {
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

    override fun list(): MutableList<DwellManifest> {
        val out = mutableListOf<DwellManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellManifestStore(): DwellManifestStore {
    return MemoryDwellManifestStore(mutableMapOf<String, DwellManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellManifestStore(
    var inner: DwellManifestStore,
    var cache: TtlCache<DwellManifest>,
    var hits: Int,
    var misses: Int,
) : DwellManifestStore {
    override fun find(code: String): DwellManifest? {
        val hit = (cache.get(code) as DwellManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellManifest> = inner.list()
}

fun newCachedDwellManifestStore(inner: DwellManifestStore, ttl: Int, clock: Clock): DwellManifestStore {
    return CachedDwellManifestStore(
        inner = inner,
        cache = newTtlCache<DwellManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell manifests. Empty fields match all. */
data class DwellManifestQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellManifest>): MutableList<DwellManifest> {
        val matched = filter(items) { candidate: DwellManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellManifests(store: DwellManifestStore, query: DwellManifestQuery): MutableList<DwellManifest> {
    return query.apply(store.list())
}
