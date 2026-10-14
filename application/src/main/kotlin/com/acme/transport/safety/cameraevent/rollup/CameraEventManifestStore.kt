package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event manifests by code. */
interface CameraEventManifestStore {
    fun find(code: String): CameraEventManifest?
    fun save(item: CameraEventManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventManifestStore(
    var items: MutableMap<String, CameraEventManifest>,
    var order: MutableList<String>,
) : CameraEventManifestStore {
    override fun find(code: String): CameraEventManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventManifest) {
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

    override fun list(): MutableList<CameraEventManifest> {
        val out = mutableListOf<CameraEventManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventManifestStore(): CameraEventManifestStore {
    return MemoryCameraEventManifestStore(mutableMapOf<String, CameraEventManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventManifestStore(
    var inner: CameraEventManifestStore,
    var cache: TtlCache<CameraEventManifest>,
    var hits: Int,
    var misses: Int,
) : CameraEventManifestStore {
    override fun find(code: String): CameraEventManifest? {
        val hit = (cache.get(code) as CameraEventManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventManifest> = inner.list()
}

fun newCachedCameraEventManifestStore(inner: CameraEventManifestStore, ttl: Int, clock: Clock): CameraEventManifestStore {
    return CachedCameraEventManifestStore(
        inner = inner,
        cache = newTtlCache<CameraEventManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event manifests. Empty fields match all. */
data class CameraEventManifestQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventManifest>): MutableList<CameraEventManifest> {
        val matched = filter(items) { candidate: CameraEventManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventManifests(store: CameraEventManifestStore, query: CameraEventManifestQuery): MutableList<CameraEventManifest> {
    return query.apply(store.list())
}
