package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped trailer yard manifests by code. */
interface CappedTrailerYardManifestStore {
    fun find(code: String): CappedTrailerYardManifest?
    fun save(item: CappedTrailerYardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerYardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerYardManifestStore(
    var items: MutableMap<String, CappedTrailerYardManifest>,
    var order: MutableList<String>,
) : CappedTrailerYardManifestStore {
    override fun find(code: String): CappedTrailerYardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerYardManifest) {
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

    override fun list(): MutableList<CappedTrailerYardManifest> {
        val out = mutableListOf<CappedTrailerYardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerYardManifestStore(): CappedTrailerYardManifestStore {
    return MemoryCappedTrailerYardManifestStore(
        mutableMapOf<String, CappedTrailerYardManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerYardManifestStore(
    var inner: CappedTrailerYardManifestStore,
    var cache: TtlCache<CappedTrailerYardManifest>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerYardManifestStore {
    override fun find(code: String): CappedTrailerYardManifest? {
        val hit = (cache.get(code) as CappedTrailerYardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerYardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerYardManifest> = inner.list()
}

fun newCachedCappedTrailerYardManifestStore(inner: CappedTrailerYardManifestStore, ttl: Int, clock: Clock): CappedTrailerYardManifestStore {
    return CachedCappedTrailerYardManifestStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerYardManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer yard manifests. Empty fields match all. */
data class CappedTrailerYardManifestQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerYardManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CappedTrailerYardManifest>): MutableList<CappedTrailerYardManifest> {
        val matched = filter(items) { candidate: CappedTrailerYardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerYardManifests(store: CappedTrailerYardManifestStore, query: CappedTrailerYardManifestQuery): MutableList<CappedTrailerYardManifest> {
    return query.apply(store.list())
}
