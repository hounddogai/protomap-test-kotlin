package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped fuel burn manifests by code. */
interface CappedFuelBurnManifestStore {
    fun find(code: String): CappedFuelBurnManifest?
    fun save(item: CappedFuelBurnManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedFuelBurnManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedFuelBurnManifestStore(
    var items: MutableMap<String, CappedFuelBurnManifest>,
    var order: MutableList<String>,
) : CappedFuelBurnManifestStore {
    override fun find(code: String): CappedFuelBurnManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedFuelBurnManifest) {
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

    override fun list(): MutableList<CappedFuelBurnManifest> {
        val out = mutableListOf<CappedFuelBurnManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedFuelBurnManifestStore(): CappedFuelBurnManifestStore {
    return MemoryCappedFuelBurnManifestStore(
        mutableMapOf<String, CappedFuelBurnManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedFuelBurnManifestStore(
    var inner: CappedFuelBurnManifestStore,
    var cache: TtlCache<CappedFuelBurnManifest>,
    var hits: Int,
    var misses: Int,
) : CappedFuelBurnManifestStore {
    override fun find(code: String): CappedFuelBurnManifest? {
        val hit = (cache.get(code) as CappedFuelBurnManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedFuelBurnManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedFuelBurnManifest> = inner.list()
}

fun newCachedCappedFuelBurnManifestStore(inner: CappedFuelBurnManifestStore, ttl: Int, clock: Clock): CappedFuelBurnManifestStore {
    return CachedCappedFuelBurnManifestStore(
        inner = inner,
        cache = newTtlCache<CappedFuelBurnManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped fuel burn manifests. Empty fields match all. */
data class CappedFuelBurnManifestQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedFuelBurnManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedFuelBurnManifest>): MutableList<CappedFuelBurnManifest> {
        val matched = filter(items) { candidate: CappedFuelBurnManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedFuelBurnManifests(store: CappedFuelBurnManifestStore, query: CappedFuelBurnManifestQuery): MutableList<CappedFuelBurnManifest> {
    return query.apply(store.list())
}
