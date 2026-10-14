package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel manifests by code. */
interface GlobalParcelManifestStore {
    fun find(code: String): GlobalParcelManifest?
    fun save(item: GlobalParcelManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelManifestStore(
    var items: MutableMap<String, GlobalParcelManifest>,
    var order: MutableList<String>,
) : GlobalParcelManifestStore {
    override fun find(code: String): GlobalParcelManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelManifest) {
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

    override fun list(): MutableList<GlobalParcelManifest> {
        val out = mutableListOf<GlobalParcelManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelManifestStore(): GlobalParcelManifestStore {
    return MemoryGlobalParcelManifestStore(
        mutableMapOf<String, GlobalParcelManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelManifestStore(
    var inner: GlobalParcelManifestStore,
    var cache: TtlCache<GlobalParcelManifest>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelManifestStore {
    override fun find(code: String): GlobalParcelManifest? {
        val hit = (cache.get(code) as GlobalParcelManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelManifest> = inner.list()
}

fun newCachedGlobalParcelManifestStore(inner: GlobalParcelManifestStore, ttl: Int, clock: Clock): GlobalParcelManifestStore {
    return CachedGlobalParcelManifestStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel manifests. Empty fields match all. */
data class GlobalParcelManifestQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelManifest>): MutableList<GlobalParcelManifest> {
        val matched = filter(items) { candidate: GlobalParcelManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelManifests(store: GlobalParcelManifestStore, query: GlobalParcelManifestQuery): MutableList<GlobalParcelManifest> {
    return query.apply(store.list())
}
