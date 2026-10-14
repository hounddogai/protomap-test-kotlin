package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary clearance manifests by code. */
interface PrimaryClearanceManifestStore {
    fun find(code: String): PrimaryClearanceManifest?
    fun save(item: PrimaryClearanceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryClearanceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryClearanceManifestStore(
    var items: MutableMap<String, PrimaryClearanceManifest>,
    var order: MutableList<String>,
) : PrimaryClearanceManifestStore {
    override fun find(code: String): PrimaryClearanceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryClearanceManifest) {
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

    override fun list(): MutableList<PrimaryClearanceManifest> {
        val out = mutableListOf<PrimaryClearanceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryClearanceManifestStore(): PrimaryClearanceManifestStore {
    return MemoryPrimaryClearanceManifestStore(
        mutableMapOf<String, PrimaryClearanceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryClearanceManifestStore(
    var inner: PrimaryClearanceManifestStore,
    var cache: TtlCache<PrimaryClearanceManifest>,
    var hits: Int,
    var misses: Int,
) : PrimaryClearanceManifestStore {
    override fun find(code: String): PrimaryClearanceManifest? {
        val hit = (cache.get(code) as PrimaryClearanceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryClearanceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryClearanceManifest> = inner.list()
}

fun newCachedPrimaryClearanceManifestStore(inner: PrimaryClearanceManifestStore, ttl: Int, clock: Clock): PrimaryClearanceManifestStore {
    return CachedPrimaryClearanceManifestStore(
        inner = inner,
        cache = newTtlCache<PrimaryClearanceManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary clearance manifests. Empty fields match all. */
data class PrimaryClearanceManifestQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryClearanceManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryClearanceManifest>): MutableList<PrimaryClearanceManifest> {
        val matched = filter(items) { candidate: PrimaryClearanceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryClearanceManifests(store: PrimaryClearanceManifestStore, query: PrimaryClearanceManifestQuery): MutableList<PrimaryClearanceManifest> {
    return query.apply(store.list())
}
