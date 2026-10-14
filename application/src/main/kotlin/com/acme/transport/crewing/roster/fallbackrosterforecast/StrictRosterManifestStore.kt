package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster manifests by code. */
interface StrictRosterManifestStore {
    fun find(code: String): StrictRosterManifest?
    fun save(item: StrictRosterManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterManifestStore(
    var items: MutableMap<String, StrictRosterManifest>,
    var order: MutableList<String>,
) : StrictRosterManifestStore {
    override fun find(code: String): StrictRosterManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterManifest) {
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

    override fun list(): MutableList<StrictRosterManifest> {
        val out = mutableListOf<StrictRosterManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterManifestStore(): StrictRosterManifestStore {
    return MemoryStrictRosterManifestStore(
        mutableMapOf<String, StrictRosterManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterManifestStore(
    var inner: StrictRosterManifestStore,
    var cache: TtlCache<StrictRosterManifest>,
    var hits: Int,
    var misses: Int,
) : StrictRosterManifestStore {
    override fun find(code: String): StrictRosterManifest? {
        val hit = (cache.get(code) as StrictRosterManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterManifest> = inner.list()
}

fun newCachedStrictRosterManifestStore(inner: StrictRosterManifestStore, ttl: Int, clock: Clock): StrictRosterManifestStore {
    return CachedStrictRosterManifestStore(
        inner = inner,
        cache = newTtlCache<StrictRosterManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster manifests. Empty fields match all. */
data class StrictRosterManifestQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterManifest>): MutableList<StrictRosterManifest> {
        val matched = filter(items) { candidate: StrictRosterManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterManifests(store: StrictRosterManifestStore, query: StrictRosterManifestQuery): MutableList<StrictRosterManifest> {
    return query.apply(store.list())
}
