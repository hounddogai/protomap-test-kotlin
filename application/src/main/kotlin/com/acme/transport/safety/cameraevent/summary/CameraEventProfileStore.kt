package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event profiles by code. */
interface CameraEventProfileStore {
    fun find(code: String): CameraEventProfile?
    fun save(item: CameraEventProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventProfileStore(
    var items: MutableMap<String, CameraEventProfile>,
    var order: MutableList<String>,
) : CameraEventProfileStore {
    override fun find(code: String): CameraEventProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventProfile) {
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

    override fun list(): MutableList<CameraEventProfile> {
        val out = mutableListOf<CameraEventProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventProfileStore(): CameraEventProfileStore {
    return MemoryCameraEventProfileStore(mutableMapOf<String, CameraEventProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventProfileStore(
    var inner: CameraEventProfileStore,
    var cache: TtlCache<CameraEventProfile>,
    var hits: Int,
    var misses: Int,
) : CameraEventProfileStore {
    override fun find(code: String): CameraEventProfile? {
        val hit = (cache.get(code) as CameraEventProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventProfile> = inner.list()
}

fun newCachedCameraEventProfileStore(inner: CameraEventProfileStore, ttl: Int, clock: Clock): CameraEventProfileStore {
    return CachedCameraEventProfileStore(
        inner = inner,
        cache = newTtlCache<CameraEventProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event profiles. Empty fields match all. */
data class CameraEventProfileQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventProfile>): MutableList<CameraEventProfile> {
        val matched = filter(items) { candidate: CameraEventProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventProfiles(store: CameraEventProfileStore, query: CameraEventProfileQuery): MutableList<CameraEventProfile> {
    return query.apply(store.list())
}
