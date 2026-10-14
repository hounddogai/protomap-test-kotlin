package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event cycles by code. */
interface CameraEventCycleStore {
    fun find(code: String): CameraEventCycle?
    fun save(item: CameraEventCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventCycleStore(
    var items: MutableMap<String, CameraEventCycle>,
    var order: MutableList<String>,
) : CameraEventCycleStore {
    override fun find(code: String): CameraEventCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventCycle) {
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

    override fun list(): MutableList<CameraEventCycle> {
        val out = mutableListOf<CameraEventCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventCycleStore(): CameraEventCycleStore {
    return MemoryCameraEventCycleStore(mutableMapOf<String, CameraEventCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventCycleStore(
    var inner: CameraEventCycleStore,
    var cache: TtlCache<CameraEventCycle>,
    var hits: Int,
    var misses: Int,
) : CameraEventCycleStore {
    override fun find(code: String): CameraEventCycle? {
        val hit = (cache.get(code) as CameraEventCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventCycle> = inner.list()
}

fun newCachedCameraEventCycleStore(inner: CameraEventCycleStore, ttl: Int, clock: Clock): CameraEventCycleStore {
    return CachedCameraEventCycleStore(
        inner = inner,
        cache = newTtlCache<CameraEventCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event cycles. Empty fields match all. */
data class CameraEventCycleQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventCycle>): MutableList<CameraEventCycle> {
        val matched = filter(items) { candidate: CameraEventCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventCycles(store: CameraEventCycleStore, query: CameraEventCycleQuery): MutableList<CameraEventCycle> {
    return query.apply(store.list())
}
