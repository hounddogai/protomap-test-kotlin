package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event digests by code. */
interface CameraEventDigestStore {
    fun find(code: String): CameraEventDigest?
    fun save(item: CameraEventDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventDigestStore(
    var items: MutableMap<String, CameraEventDigest>,
    var order: MutableList<String>,
) : CameraEventDigestStore {
    override fun find(code: String): CameraEventDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventDigest) {
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

    override fun list(): MutableList<CameraEventDigest> {
        val out = mutableListOf<CameraEventDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventDigestStore(): CameraEventDigestStore {
    return MemoryCameraEventDigestStore(mutableMapOf<String, CameraEventDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventDigestStore(
    var inner: CameraEventDigestStore,
    var cache: TtlCache<CameraEventDigest>,
    var hits: Int,
    var misses: Int,
) : CameraEventDigestStore {
    override fun find(code: String): CameraEventDigest? {
        val hit = (cache.get(code) as CameraEventDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventDigest> = inner.list()
}

fun newCachedCameraEventDigestStore(inner: CameraEventDigestStore, ttl: Int, clock: Clock): CameraEventDigestStore {
    return CachedCameraEventDigestStore(
        inner = inner,
        cache = newTtlCache<CameraEventDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event digests. Empty fields match all. */
data class CameraEventDigestQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventDigest>): MutableList<CameraEventDigest> {
        val matched = filter(items) { candidate: CameraEventDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventDigests(store: CameraEventDigestStore, query: CameraEventDigestQuery): MutableList<CameraEventDigest> {
    return query.apply(store.list())
}
