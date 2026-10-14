package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event targets by code. */
interface CameraEventTargetStore {
    fun find(code: String): CameraEventTarget?
    fun save(item: CameraEventTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventTargetStore(
    var items: MutableMap<String, CameraEventTarget>,
    var order: MutableList<String>,
) : CameraEventTargetStore {
    override fun find(code: String): CameraEventTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventTarget) {
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

    override fun list(): MutableList<CameraEventTarget> {
        val out = mutableListOf<CameraEventTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventTargetStore(): CameraEventTargetStore {
    return MemoryCameraEventTargetStore(mutableMapOf<String, CameraEventTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventTargetStore(
    var inner: CameraEventTargetStore,
    var cache: TtlCache<CameraEventTarget>,
    var hits: Int,
    var misses: Int,
) : CameraEventTargetStore {
    override fun find(code: String): CameraEventTarget? {
        val hit = (cache.get(code) as CameraEventTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventTarget> = inner.list()
}

fun newCachedCameraEventTargetStore(inner: CameraEventTargetStore, ttl: Int, clock: Clock): CameraEventTargetStore {
    return CachedCameraEventTargetStore(
        inner = inner,
        cache = newTtlCache<CameraEventTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event targets. Empty fields match all. */
data class CameraEventTargetQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventTarget): Boolean {
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

    fun apply(items: MutableList<CameraEventTarget>): MutableList<CameraEventTarget> {
        val matched = filter(items) { candidate: CameraEventTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventTargets(store: CameraEventTargetStore, query: CameraEventTargetQuery): MutableList<CameraEventTarget> {
    return query.apply(store.list())
}
