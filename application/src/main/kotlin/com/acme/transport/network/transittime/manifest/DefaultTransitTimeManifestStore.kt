package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time manifests by code. */
interface DefaultTransitTimeManifestStore {
    fun find(code: String): DefaultTransitTimeManifest?
    fun save(item: DefaultTransitTimeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimeManifestStore(
    var items: MutableMap<String, DefaultTransitTimeManifest>,
    var order: MutableList<String>,
) : DefaultTransitTimeManifestStore {
    override fun find(code: String): DefaultTransitTimeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimeManifest) {
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

    override fun list(): MutableList<DefaultTransitTimeManifest> {
        val out = mutableListOf<DefaultTransitTimeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimeManifestStore(): DefaultTransitTimeManifestStore {
    return MemoryDefaultTransitTimeManifestStore(
        mutableMapOf<String, DefaultTransitTimeManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimeManifestStore(
    var inner: DefaultTransitTimeManifestStore,
    var cache: TtlCache<DefaultTransitTimeManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimeManifestStore {
    override fun find(code: String): DefaultTransitTimeManifest? {
        val hit = (cache.get(code) as DefaultTransitTimeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimeManifest> = inner.list()
}

fun newCachedDefaultTransitTimeManifestStore(inner: DefaultTransitTimeManifestStore, ttl: Int, clock: Clock): DefaultTransitTimeManifestStore {
    return CachedDefaultTransitTimeManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimeManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time manifests. Empty fields match all. */
data class DefaultTransitTimeManifestQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimeManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTransitTimeManifest>): MutableList<DefaultTransitTimeManifest> {
        val matched = filter(items) { candidate: DefaultTransitTimeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimeManifests(store: DefaultTransitTimeManifestStore, query: DefaultTransitTimeManifestQuery): MutableList<DefaultTransitTimeManifest> {
    return query.apply(store.list())
}
