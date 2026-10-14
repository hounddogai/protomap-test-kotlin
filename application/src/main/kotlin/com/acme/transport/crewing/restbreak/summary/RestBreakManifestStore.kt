package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break manifests by code. */
interface RestBreakManifestStore {
    fun find(code: String): RestBreakManifest?
    fun save(item: RestBreakManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakManifestStore(
    var items: MutableMap<String, RestBreakManifest>,
    var order: MutableList<String>,
) : RestBreakManifestStore {
    override fun find(code: String): RestBreakManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakManifest) {
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

    override fun list(): MutableList<RestBreakManifest> {
        val out = mutableListOf<RestBreakManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakManifestStore(): RestBreakManifestStore {
    return MemoryRestBreakManifestStore(mutableMapOf<String, RestBreakManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakManifestStore(
    var inner: RestBreakManifestStore,
    var cache: TtlCache<RestBreakManifest>,
    var hits: Int,
    var misses: Int,
) : RestBreakManifestStore {
    override fun find(code: String): RestBreakManifest? {
        val hit = (cache.get(code) as RestBreakManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakManifest> = inner.list()
}

fun newCachedRestBreakManifestStore(inner: RestBreakManifestStore, ttl: Int, clock: Clock): RestBreakManifestStore {
    return CachedRestBreakManifestStore(
        inner = inner,
        cache = newTtlCache<RestBreakManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break manifests. Empty fields match all. */
data class RestBreakManifestQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakManifest>): MutableList<RestBreakManifest> {
        val matched = filter(items) { candidate: RestBreakManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakManifests(store: RestBreakManifestStore, query: RestBreakManifestQuery): MutableList<RestBreakManifest> {
    return query.apply(store.list())
}
