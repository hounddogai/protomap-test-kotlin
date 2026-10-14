package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event claims by code. */
interface CameraEventClaimStore {
    fun find(code: String): CameraEventClaim?
    fun save(item: CameraEventClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventClaimStore(
    var items: MutableMap<String, CameraEventClaim>,
    var order: MutableList<String>,
) : CameraEventClaimStore {
    override fun find(code: String): CameraEventClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventClaim) {
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

    override fun list(): MutableList<CameraEventClaim> {
        val out = mutableListOf<CameraEventClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventClaimStore(): CameraEventClaimStore {
    return MemoryCameraEventClaimStore(mutableMapOf<String, CameraEventClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventClaimStore(
    var inner: CameraEventClaimStore,
    var cache: TtlCache<CameraEventClaim>,
    var hits: Int,
    var misses: Int,
) : CameraEventClaimStore {
    override fun find(code: String): CameraEventClaim? {
        val hit = (cache.get(code) as CameraEventClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventClaim> = inner.list()
}

fun newCachedCameraEventClaimStore(inner: CameraEventClaimStore, ttl: Int, clock: Clock): CameraEventClaimStore {
    return CachedCameraEventClaimStore(
        inner = inner,
        cache = newTtlCache<CameraEventClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event claims. Empty fields match all. */
data class CameraEventClaimQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<CameraEventClaim>): MutableList<CameraEventClaim> {
        val matched = filter(items) { candidate: CameraEventClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventClaims(store: CameraEventClaimStore, query: CameraEventClaimQuery): MutableList<CameraEventClaim> {
    return query.apply(store.list())
}
