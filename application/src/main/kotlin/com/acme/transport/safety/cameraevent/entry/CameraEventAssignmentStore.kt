package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event assignments by code. */
interface CameraEventAssignmentStore {
    fun find(code: String): CameraEventAssignment?
    fun save(item: CameraEventAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventAssignmentStore(
    var items: MutableMap<String, CameraEventAssignment>,
    var order: MutableList<String>,
) : CameraEventAssignmentStore {
    override fun find(code: String): CameraEventAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventAssignment) {
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

    override fun list(): MutableList<CameraEventAssignment> {
        val out = mutableListOf<CameraEventAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventAssignmentStore(): CameraEventAssignmentStore {
    return MemoryCameraEventAssignmentStore(
        mutableMapOf<String, CameraEventAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventAssignmentStore(
    var inner: CameraEventAssignmentStore,
    var cache: TtlCache<CameraEventAssignment>,
    var hits: Int,
    var misses: Int,
) : CameraEventAssignmentStore {
    override fun find(code: String): CameraEventAssignment? {
        val hit = (cache.get(code) as CameraEventAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventAssignment> = inner.list()
}

fun newCachedCameraEventAssignmentStore(inner: CameraEventAssignmentStore, ttl: Int, clock: Clock): CameraEventAssignmentStore {
    return CachedCameraEventAssignmentStore(
        inner = inner,
        cache = newTtlCache<CameraEventAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event assignments. Empty fields match all. */
data class CameraEventAssignmentQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventAssignment>): MutableList<CameraEventAssignment> {
        val matched = filter(items) { candidate: CameraEventAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventAssignments(store: CameraEventAssignmentStore, query: CameraEventAssignmentQuery): MutableList<CameraEventAssignment> {
    return query.apply(store.list())
}
