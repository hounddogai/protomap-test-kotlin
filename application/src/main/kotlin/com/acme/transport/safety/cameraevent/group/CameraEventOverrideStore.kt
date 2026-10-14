package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event overrides by code. */
interface CameraEventOverrideStore {
    fun find(code: String): CameraEventOverride?
    fun save(item: CameraEventOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventOverrideStore(
    var items: MutableMap<String, CameraEventOverride>,
    var order: MutableList<String>,
) : CameraEventOverrideStore {
    override fun find(code: String): CameraEventOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventOverride) {
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

    override fun list(): MutableList<CameraEventOverride> {
        val out = mutableListOf<CameraEventOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventOverrideStore(): CameraEventOverrideStore {
    return MemoryCameraEventOverrideStore(mutableMapOf<String, CameraEventOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventOverrideStore(
    var inner: CameraEventOverrideStore,
    var cache: TtlCache<CameraEventOverride>,
    var hits: Int,
    var misses: Int,
) : CameraEventOverrideStore {
    override fun find(code: String): CameraEventOverride? {
        val hit = (cache.get(code) as CameraEventOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventOverride> = inner.list()
}

fun newCachedCameraEventOverrideStore(inner: CameraEventOverrideStore, ttl: Int, clock: Clock): CameraEventOverrideStore {
    return CachedCameraEventOverrideStore(
        inner = inner,
        cache = newTtlCache<CameraEventOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event overrides. Empty fields match all. */
data class CameraEventOverrideQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventOverride>): MutableList<CameraEventOverride> {
        val matched = filter(items) { candidate: CameraEventOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventOverrides(store: CameraEventOverrideStore, query: CameraEventOverrideQuery): MutableList<CameraEventOverride> {
    return query.apply(store.list())
}
