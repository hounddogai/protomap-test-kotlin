package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict sortation manifests by code. */
interface StrictSortationManifestStore {
    fun find(code: String): StrictSortationManifest?
    fun save(item: StrictSortationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictSortationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictSortationManifestStore(
    var items: MutableMap<String, StrictSortationManifest>,
    var order: MutableList<String>,
) : StrictSortationManifestStore {
    override fun find(code: String): StrictSortationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictSortationManifest) {
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

    override fun list(): MutableList<StrictSortationManifest> {
        val out = mutableListOf<StrictSortationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictSortationManifestStore(): StrictSortationManifestStore {
    return MemoryStrictSortationManifestStore(
        mutableMapOf<String, StrictSortationManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictSortationManifestStore(
    var inner: StrictSortationManifestStore,
    var cache: TtlCache<StrictSortationManifest>,
    var hits: Int,
    var misses: Int,
) : StrictSortationManifestStore {
    override fun find(code: String): StrictSortationManifest? {
        val hit = (cache.get(code) as StrictSortationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictSortationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictSortationManifest> = inner.list()
}

fun newCachedStrictSortationManifestStore(inner: StrictSortationManifestStore, ttl: Int, clock: Clock): StrictSortationManifestStore {
    return CachedStrictSortationManifestStore(
        inner = inner,
        cache = newTtlCache<StrictSortationManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict sortation manifests. Empty fields match all. */
data class StrictSortationManifestQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictSortationManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictSortationManifest>): MutableList<StrictSortationManifest> {
        val matched = filter(items) { candidate: StrictSortationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictSortationManifests(store: StrictSortationManifestStore, query: StrictSortationManifestQuery): MutableList<StrictSortationManifest> {
    return query.apply(store.list())
}
