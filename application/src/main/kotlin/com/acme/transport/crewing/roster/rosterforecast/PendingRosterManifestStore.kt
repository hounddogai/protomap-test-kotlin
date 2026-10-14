package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending roster manifests by code. */
interface PendingRosterManifestStore {
    fun find(code: String): PendingRosterManifest?
    fun save(item: PendingRosterManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRosterManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRosterManifestStore(
    var items: MutableMap<String, PendingRosterManifest>,
    var order: MutableList<String>,
) : PendingRosterManifestStore {
    override fun find(code: String): PendingRosterManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRosterManifest) {
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

    override fun list(): MutableList<PendingRosterManifest> {
        val out = mutableListOf<PendingRosterManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRosterManifestStore(): PendingRosterManifestStore {
    return MemoryPendingRosterManifestStore(
        mutableMapOf<String, PendingRosterManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRosterManifestStore(
    var inner: PendingRosterManifestStore,
    var cache: TtlCache<PendingRosterManifest>,
    var hits: Int,
    var misses: Int,
) : PendingRosterManifestStore {
    override fun find(code: String): PendingRosterManifest? {
        val hit = (cache.get(code) as PendingRosterManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRosterManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRosterManifest> = inner.list()
}

fun newCachedPendingRosterManifestStore(inner: PendingRosterManifestStore, ttl: Int, clock: Clock): PendingRosterManifestStore {
    return CachedPendingRosterManifestStore(
        inner = inner,
        cache = newTtlCache<PendingRosterManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending roster manifests. Empty fields match all. */
data class PendingRosterManifestQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRosterManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRosterManifest>): MutableList<PendingRosterManifest> {
        val matched = filter(items) { candidate: PendingRosterManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRosterManifests(store: PendingRosterManifestStore, query: PendingRosterManifestQuery): MutableList<PendingRosterManifest> {
    return query.apply(store.list())
}
