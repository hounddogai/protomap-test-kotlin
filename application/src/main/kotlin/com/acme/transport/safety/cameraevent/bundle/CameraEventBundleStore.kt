package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event bundles by code. */
interface CameraEventBundleStore {
    fun find(code: String): CameraEventBundle?
    fun save(item: CameraEventBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventBundleStore(
    var items: MutableMap<String, CameraEventBundle>,
    var order: MutableList<String>,
) : CameraEventBundleStore {
    override fun find(code: String): CameraEventBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventBundle) {
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

    override fun list(): MutableList<CameraEventBundle> {
        val out = mutableListOf<CameraEventBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventBundleStore(): CameraEventBundleStore {
    return MemoryCameraEventBundleStore(mutableMapOf<String, CameraEventBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventBundleStore(
    var inner: CameraEventBundleStore,
    var cache: TtlCache<CameraEventBundle>,
    var hits: Int,
    var misses: Int,
) : CameraEventBundleStore {
    override fun find(code: String): CameraEventBundle? {
        val hit = (cache.get(code) as CameraEventBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventBundle> = inner.list()
}

fun newCachedCameraEventBundleStore(inner: CameraEventBundleStore, ttl: Int, clock: Clock): CameraEventBundleStore {
    return CachedCameraEventBundleStore(
        inner = inner,
        cache = newTtlCache<CameraEventBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event bundles. Empty fields match all. */
data class CameraEventBundleQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventBundle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventBundle>): MutableList<CameraEventBundle> {
        val matched = filter(items) { candidate: CameraEventBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventBundles(store: CameraEventBundleStore, query: CameraEventBundleQuery): MutableList<CameraEventBundle> {
    return query.apply(store.list())
}
