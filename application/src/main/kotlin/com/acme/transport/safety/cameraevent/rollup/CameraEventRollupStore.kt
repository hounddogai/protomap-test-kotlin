package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event rollups by code. */
interface CameraEventRollupStore {
    fun find(code: String): CameraEventRollup?
    fun save(item: CameraEventRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventRollupStore(
    var items: MutableMap<String, CameraEventRollup>,
    var order: MutableList<String>,
) : CameraEventRollupStore {
    override fun find(code: String): CameraEventRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventRollup) {
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

    override fun list(): MutableList<CameraEventRollup> {
        val out = mutableListOf<CameraEventRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventRollupStore(): CameraEventRollupStore {
    return MemoryCameraEventRollupStore(mutableMapOf<String, CameraEventRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventRollupStore(
    var inner: CameraEventRollupStore,
    var cache: TtlCache<CameraEventRollup>,
    var hits: Int,
    var misses: Int,
) : CameraEventRollupStore {
    override fun find(code: String): CameraEventRollup? {
        val hit = (cache.get(code) as CameraEventRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventRollup> = inner.list()
}

fun newCachedCameraEventRollupStore(inner: CameraEventRollupStore, ttl: Int, clock: Clock): CameraEventRollupStore {
    return CachedCameraEventRollupStore(
        inner = inner,
        cache = newTtlCache<CameraEventRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event rollups. Empty fields match all. */
data class CameraEventRollupQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CameraEventRollup>): MutableList<CameraEventRollup> {
        val matched = filter(items) { candidate: CameraEventRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventRollups(store: CameraEventRollupStore, query: CameraEventRollupQuery): MutableList<CameraEventRollup> {
    return query.apply(store.list())
}
