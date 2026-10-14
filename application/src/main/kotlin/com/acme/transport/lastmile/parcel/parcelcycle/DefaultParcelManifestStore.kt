package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel manifests by code. */
interface DefaultParcelManifestStore {
    fun find(code: String): DefaultParcelManifest?
    fun save(item: DefaultParcelManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelManifestStore(
    var items: MutableMap<String, DefaultParcelManifest>,
    var order: MutableList<String>,
) : DefaultParcelManifestStore {
    override fun find(code: String): DefaultParcelManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelManifest) {
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

    override fun list(): MutableList<DefaultParcelManifest> {
        val out = mutableListOf<DefaultParcelManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelManifestStore(): DefaultParcelManifestStore {
    return MemoryDefaultParcelManifestStore(
        mutableMapOf<String, DefaultParcelManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelManifestStore(
    var inner: DefaultParcelManifestStore,
    var cache: TtlCache<DefaultParcelManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelManifestStore {
    override fun find(code: String): DefaultParcelManifest? {
        val hit = (cache.get(code) as DefaultParcelManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelManifest> = inner.list()
}

fun newCachedDefaultParcelManifestStore(inner: DefaultParcelManifestStore, ttl: Int, clock: Clock): DefaultParcelManifestStore {
    return CachedDefaultParcelManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel manifests. Empty fields match all. */
data class DefaultParcelManifestQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelManifest>): MutableList<DefaultParcelManifest> {
        val matched = filter(items) { candidate: DefaultParcelManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelManifests(store: DefaultParcelManifestStore, query: DefaultParcelManifestQuery): MutableList<DefaultParcelManifest> {
    return query.apply(store.list())
}
