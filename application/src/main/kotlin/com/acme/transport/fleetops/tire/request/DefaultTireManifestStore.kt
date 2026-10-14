package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tire manifests by code. */
interface DefaultTireManifestStore {
    fun find(code: String): DefaultTireManifest?
    fun save(item: DefaultTireManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTireManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTireManifestStore(
    var items: MutableMap<String, DefaultTireManifest>,
    var order: MutableList<String>,
) : DefaultTireManifestStore {
    override fun find(code: String): DefaultTireManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTireManifest) {
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

    override fun list(): MutableList<DefaultTireManifest> {
        val out = mutableListOf<DefaultTireManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTireManifestStore(): DefaultTireManifestStore {
    return MemoryDefaultTireManifestStore(mutableMapOf<String, DefaultTireManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTireManifestStore(
    var inner: DefaultTireManifestStore,
    var cache: TtlCache<DefaultTireManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultTireManifestStore {
    override fun find(code: String): DefaultTireManifest? {
        val hit = (cache.get(code) as DefaultTireManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTireManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTireManifest> = inner.list()
}

fun newCachedDefaultTireManifestStore(inner: DefaultTireManifestStore, ttl: Int, clock: Clock): DefaultTireManifestStore {
    return CachedDefaultTireManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultTireManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tire manifests. Empty fields match all. */
data class DefaultTireManifestQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTireManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTireManifest>): MutableList<DefaultTireManifest> {
        val matched = filter(items) { candidate: DefaultTireManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTireManifests(store: DefaultTireManifestStore, query: DefaultTireManifestQuery): MutableList<DefaultTireManifest> {
    return query.apply(store.list())
}
