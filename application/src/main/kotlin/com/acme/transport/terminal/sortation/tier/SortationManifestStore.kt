package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation manifests by code. */
interface SortationManifestStore {
    fun find(code: String): SortationManifest?
    fun save(item: SortationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationManifestStore(
    var items: MutableMap<String, SortationManifest>,
    var order: MutableList<String>,
) : SortationManifestStore {
    override fun find(code: String): SortationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationManifest) {
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

    override fun list(): MutableList<SortationManifest> {
        val out = mutableListOf<SortationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationManifestStore(): SortationManifestStore {
    return MemorySortationManifestStore(mutableMapOf<String, SortationManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationManifestStore(
    var inner: SortationManifestStore,
    var cache: TtlCache<SortationManifest>,
    var hits: Int,
    var misses: Int,
) : SortationManifestStore {
    override fun find(code: String): SortationManifest? {
        val hit = (cache.get(code) as SortationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationManifest> = inner.list()
}

fun newCachedSortationManifestStore(inner: SortationManifestStore, ttl: Int, clock: Clock): SortationManifestStore {
    return CachedSortationManifestStore(
        inner = inner,
        cache = newTtlCache<SortationManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation manifests. Empty fields match all. */
data class SortationManifestQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SortationManifest>): MutableList<SortationManifest> {
        val matched = filter(items) { candidate: SortationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationManifests(store: SortationManifestStore, query: SortationManifestQuery): MutableList<SortationManifest> {
    return query.apply(store.list())
}
