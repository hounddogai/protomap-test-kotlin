package com.acme.transport.network.hub.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub manifests by code. */
interface HubManifestStore {
    fun find(code: String): HubManifest?
    fun save(item: HubManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubManifestStore(
    var items: MutableMap<String, HubManifest>,
    var order: MutableList<String>,
) : HubManifestStore {
    override fun find(code: String): HubManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubManifest) {
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

    override fun list(): MutableList<HubManifest> {
        val out = mutableListOf<HubManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubManifestStore(): HubManifestStore {
    return MemoryHubManifestStore(mutableMapOf<String, HubManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubManifestStore(
    var inner: HubManifestStore,
    var cache: TtlCache<HubManifest>,
    var hits: Int,
    var misses: Int,
) : HubManifestStore {
    override fun find(code: String): HubManifest? {
        val hit = (cache.get(code) as HubManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubManifest> = inner.list()
}

fun newCachedHubManifestStore(inner: HubManifestStore, ttl: Int, clock: Clock): HubManifestStore {
    return CachedHubManifestStore(
        inner = inner,
        cache = newTtlCache<HubManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub manifests. Empty fields match all. */
data class HubManifestQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubManifest>): MutableList<HubManifest> {
        val matched = filter(items) { candidate: HubManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubManifests(store: HubManifestStore, query: HubManifestQuery): MutableList<HubManifest> {
    return query.apply(store.list())
}
